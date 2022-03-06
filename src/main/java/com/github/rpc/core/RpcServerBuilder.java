package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import com.github.rpc.invoke.MethodInvokeListener;
import com.github.rpc.plugins.limit.RateLimitInterceptor;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.StringUtil;

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

    public <T> RpcServerBuilder setNettyChildOption(ChannelOption<T> childOption, T value) {
        this.rpcServer.setChildOption(childOption, value);
        return this;
    }

    public <T> RpcServerBuilder setNettyServerOption(ChannelOption<T> childOption, T value) {
        this.rpcServer.setServerOption(childOption, value);
        return this;
    }

    public RpcServer build() {

        if (StringUtil.isNullOrEmpty(packageName) && components.size() == 0) {
            throw new IllegalStateException("packageName is empty and no item in components");
        }

        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner(this.packageName, configuration);
        scanner.registerScanClass(this.components.toArray(new Class[]{}));
        scanner.scan();

        MethodInvokeDispatcherBuilder dispatcherBuilder = new MethodInvokeDispatcherBuilder(configuration);
        // 保存asm生成字节码
        if (this.saveAsmByteCode) {
            dispatcherBuilder = dispatcherBuilder.enableSaveAsmByteCode();
        }

        MethodInvokeDispatcher dispatcher = dispatcherBuilder
                .invokeType(this.type)
                .build();

        // 添加方法调用监听器
        this.listeners.forEach(dispatcher::addInvokeListener);

        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();
        if (rateLimitEntryMap.size() > 0) {
            dispatcher = new RateLimitInterceptor(rateLimitEntryMap).apply(dispatcher);
        }

        this.rpcServer.setDispatcher(dispatcher);
        return rpcServer;
    }

}
