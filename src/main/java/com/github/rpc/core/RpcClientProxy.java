package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.loadbalance.LoadBalanceStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/6 10:55
 */
public class RpcClientProxy {

    private final Map<Method, String> cache = new HashMap<>();
    private final RpcClient rpcClient;
    private boolean start;
    private boolean loadBalanceFlag;

    public RpcClientProxy(InetSocketAddress address) {
        this.rpcClient = new RpcClientImpl(address);
    }

    /**
     * 负载均衡
     *
     * @param addressList 服务端地址
     * @param strategy    负载策略
     */
    public RpcClientProxy(List<InetSocketAddress> addressList, LoadBalanceStrategy strategy) {
        this.rpcClient = new RpcLoadBalanceClientImpl(addressList, strategy);
        this.loadBalanceFlag = true;
    }

    public <T> T createProxy(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        // 非负载均衡模式
        if (!start && !loadBalanceFlag) {
            this.startClient();
        }

        com.github.rpc.annotation.RpcClient annotation = type.getAnnotation(com.github.rpc.annotation.RpcClient.class);
        if (annotation == null) {
            throw new IllegalStateException("not found RpcClient annotation in " + type.getSimpleName());
        }

        ClassLoader classLoader = this.getClass().getClassLoader();
        Object instance = Proxy.newProxyInstance(classLoader, new Class[]{type}, (proxy, method, args) -> {
            String name = cache.computeIfAbsent(method, m -> annotation.value() + m.getName());
            return rpcClient.invoke(name, args, method.getReturnType());
        });

        return type.cast(instance);
    }

    public <T> List<T> createProxyByList(List<Class<T>> typeList) {
        if (typeList == null) {
            throw new IllegalStateException("typeList cannot be null");
        }

        List<T> results = new ArrayList<>(typeList.size());
        typeList.forEach(type -> results.add(createProxy(type)));

        return results;
    }

    private void startClient() {
        new Thread(() -> {
            try {
                this.rpcClient.start();
                this.start = true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();

        this.waitStartClient();
    }

    private void waitStartClient() {
        while (!this.rpcClient.isRunning()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
