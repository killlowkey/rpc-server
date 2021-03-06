package com.github.rpc.core;

import cn.hutool.core.net.NetUtil;
import com.github.rpc.RpcServer;
import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import com.github.rpc.invoke.MethodInvokeListener;
import com.github.rpc.plugins.health.HealthRequestInterceptor;
import com.github.rpc.plugins.limit.RateLimitInterceptor;
import com.github.rpc.plugins.statistic.MethodInvokeStatistics;
import com.github.rpc.plugins.statistic.Storage;
import com.github.rpc.registry.RegisterMode;
import com.github.rpc.registry.Registry;
import com.github.rpc.registry.RegistryConfig;
import com.github.rpc.registry.RegistryFactory;
import com.github.rpc.registry.zookeeper.ZookeeperRegistry;
import com.github.rpc.serializer.Serializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.StringUtil;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/5 19:10
 */
public class RpcServerBuilder {
    private String packageName;
    private InvokeType type = InvokeType.ASM;
    private boolean saveAsmByteCode;
    private final List<Class<?>> components = new ArrayList<>();
    private final RpcServerImpl rpcServer = new RpcServerImpl();
    private final List<MethodInvokeListener> listeners = new ArrayList<>();
    private RegisterMode registerMode;
    private final RegistryConfig config = new RegistryConfig();

    public RpcServerBuilder scanPackage(String packageName) {
        if (StringUtil.isNullOrEmpty(packageName)) {
            throw new IllegalArgumentException("packageName cannot be empty");
        }

        this.packageName = packageName;
        return this;
    }

    public RpcServerBuilder registerComponent(Class<?>... components) {
        this.components.addAll(Arrays.asList(components));
        return this;
    }

    public RpcServerBuilder addMethodInvokeListener(MethodInvokeListener... listener) {
        this.listeners.addAll(Arrays.asList(listener));
        return this;
    }

    public RpcServerBuilder invokeType(InvokeType type) {
        if (type == null) {
            throw new IllegalArgumentException("invokeType cannot be null");
        }

        this.type = type;
        return this;
    }

    public RpcServerBuilder enableSaveAsmByteCode() {
        this.saveAsmByteCode = true;
        return this;
    }

    public RpcServerBuilder bind(int port) {
        if (port < 0) {
            throw new IllegalStateException("port needs to be greater than 0");
        }

        this.rpcServer.setAddress(new InetSocketAddress(port));
        return this;
    }

    public RpcServerBuilder bind(InetSocketAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }

        this.rpcServer.setAddress(address);
        return this;
    }

    public <T> RpcServerBuilder nettyChildOption(ChannelOption<T> childOption, T value) {
        this.rpcServer.childOption(childOption, value);
        return this;
    }

    public <T> RpcServerBuilder nettyServerOption(ChannelOption<T> childOption, T value) {
        this.rpcServer.serverOption(childOption, value);
        return this;
    }

    public RpcServerBuilder enableInvocationStatistics(Storage storage) {
        MethodInvokeStatistics statistics = new MethodInvokeStatistics(storage);
        this.addMethodInvokeListener(statistics);
        return this;
    }

    public RpcServerBuilder enableSSL(File jksFile, String keyStorePass, boolean needClientAuth) {
        this.rpcServer.enableSsl(jksFile, keyStorePass, needClientAuth);
        return this;
    }

    public RpcServerBuilder serialize(Serializer serializer) {
        this.rpcServer.serialize(serializer);
        return this;
    }

    public RpcServerBuilder zookeeper(String zookeeperAddress) {
        this.registerMode = RegisterMode.ZOOKEEPER;
        this.config.put(ZookeeperRegistry.ZOOKEEPER_KEY, zookeeperAddress);
        return this;
    }

    public RpcServer build() {

        if (StringUtil.isNullOrEmpty(packageName) && components.size() == 0) {
            throw new IllegalStateException("packageName is empty and no item in components");
        }

        // ????????????
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner(this.packageName, configuration);
        scanner.registerScanClass(this.components.toArray(new Class[]{}));
        scanner.scan();

        // ????????????
        registerMode = registerMode == null ? RegisterMode.MEMORY : registerMode;
        Registry registry = RegistryFactory.getRegistry(registerMode, config);
        configuration.setRegistry(registry);
        rpcServer.setRegistry(registry);

        // ????????????
        int port = this.rpcServer.getAddress().getPort();
        InetSocketAddress address = new InetSocketAddress(NetUtil.getLocalhost(), port);
        configuration.setAddress(address);

        MethodInvokeDispatcherBuilder dispatcherBuilder = new MethodInvokeDispatcherBuilder(configuration);
        // ??????asm???????????????
        if (this.saveAsmByteCode) {
            dispatcherBuilder = dispatcherBuilder.enableSaveAsmByteCode();
        }

        MethodInvokeDispatcher dispatcher = dispatcherBuilder
                .invokeType(this.type)
                .build();

        // ???????????????????????????
        this.listeners.forEach(dispatcher::addInvokeListener);

        // ????????????
        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();
        if (rateLimitEntryMap.size() > 0) {
            dispatcher = new RateLimitInterceptor(rateLimitEntryMap).apply(dispatcher);
        }

        // ????????????
        dispatcher = new HealthRequestInterceptor().apply(dispatcher);

        this.rpcServer.setDispatcher(dispatcher);
        return rpcServer;
    }

}
