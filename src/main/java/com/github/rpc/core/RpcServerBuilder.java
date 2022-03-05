package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import com.github.rpc.invoke.MethodInvokeListener;
import com.github.rpc.plugins.limit.RateLimitInterceptor;
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

    private InetSocketAddress address;
    private String packageName;
    private InvokeType type = InvokeType.ASM;
    private final List<Class<?>> components = new ArrayList<>();
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

    public RpcServerBuilder bind(int port) {
        if (port < 0) {
            throw new IllegalStateException("port needs to be greater than 0");
        }

        this.address = new InetSocketAddress(port);
        return this;
    }

    public RpcServerBuilder bind(InetSocketAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }

        this.address = address;
        return this;
    }

    public RpcServer build() {

        if (StringUtil.isNullOrEmpty(packageName) && components.size() == 0) {
            throw new IllegalStateException("packageName is empty and no item in components");
        }

        if (this.address == null) {
            throw new IllegalStateException("address cannot be null");
        }

        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner(this.packageName, configuration);
        scanner.registerScanClass(this.components.toArray(new Class[]{}));
        scanner.scan();

        MethodInvokeDispatcher dispatcher = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(this.type)
                .build();
        // 添加方法调用监听器
        this.listeners.forEach(dispatcher::addInvokeListener);

        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();
        if (rateLimitEntryMap.size() > 0) {
            dispatcher = new RateLimitInterceptor(rateLimitEntryMap).apply(dispatcher);
        }

        return new RpcServerImpl(dispatcher, this.address);
    }

}
