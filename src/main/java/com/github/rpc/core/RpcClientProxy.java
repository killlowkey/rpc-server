package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import com.github.rpc.registry.*;
import com.github.rpc.registry.memory.MemoryRegistry;
import com.github.rpc.registry.zookeeper.ZookeeperRegistry;
import com.github.rpc.serializer.Serializer;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/6 10:55
 */
public class RpcClientProxy {

    private final Map<Method, String> cache = new HashMap<>();
    private RpcClient rpcClient;
    private boolean start;

    private RegisterMode mode;
    private ServiceDiscovery discovery;
    private LoadBalanceStrategy strategy;
    private final RegistryConfig config = new RegistryConfig();
    private final List<Consumer<RpcClient>> rpcClientFunctions = new ArrayList<>();
    private NotifyListener listener;

    public RpcClientProxy(InetSocketAddress address) {
        this.rpcClient = new RpcClientImpl(address);
    }

    public RpcClientProxy() {

    }

    public RpcClientProxy zookeeper(String zookeeperAddress) {
        this.mode = RegisterMode.ZOOKEEPER;
        config.put(ZookeeperRegistry.ZOOKEEPER_KEY, zookeeperAddress);
        return this;
    }

    public RpcClientProxy rpcServerAddress(List<InetSocketAddress> addresses) {
        this.mode = RegisterMode.MEMORY;
        config.put(MemoryRegistry.RPC_SERVER_ADDRESS, addresses);
        return this;
    }


    public RpcClientProxy enableSsl(File jksFile, String keyStorePass) {
        this.rpcClientFunctions.add(client -> client.enableSsl(jksFile, keyStorePass, false));
        return this;
    }

    public RpcClientProxy serialize(Serializer serializer) {
        this.rpcClientFunctions.add(client -> client.setSerializer(serializer));
        return this;
    }

    public RpcClientProxy loadBalance(LoadBalanceStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public <T> T createProxy(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        if (!this.start) {
            init();
        }


        com.github.rpc.annotation.RpcClient annotation = type.getAnnotation(com.github.rpc.annotation.RpcClient.class);
        if (annotation == null) {
            throw new IllegalStateException("not found RpcClient annotation in " + type.getSimpleName());
        }

        // 订阅服务
        String serviceName = Strings.isNullOrEmpty(annotation.value()) ? type.getName() : annotation.value();
        this.discovery.subscribe(serviceName, listener);

        ClassLoader classLoader = this.getClass().getClassLoader();
        Object instance = Proxy.newProxyInstance(classLoader, new Class[]{type}, (proxy, method, args) -> {
            String name = cache.computeIfAbsent(method, m -> {
                String value = annotation.value();
                // 设置别名
                if (!Strings.isNullOrEmpty(value)) {
                    return value + "#" + m.getName();
                } else {
                    // 接口名称 + # + 方法名称
                    return type.getName() + "#" + m.getName();
                }
            });

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

    private void init() {
        // 无负载均衡
        if (this.rpcClient != null) {
            // 启动客户端
            startRpcClient(this.rpcClient);
            start = true;
            return;
        }

        if (config.isEmpty()) {
            throw new IllegalStateException("not found config");
        }

        this.mode = mode == null ? RegisterMode.MEMORY : mode;
        this.strategy = strategy == null ? LoadBalanceStrategy.ROTATION : strategy;
        this.discovery = ServiceDiscoveryFactory.getServiceDiscovery(mode, config);
        this.rpcClient = new RpcLoadBalanceClientImpl(discovery, strategy);
        this.listener = new NotifyListenerImpl((RpcLoadBalanceClientImpl) rpcClient);
    }

    class NotifyListenerImpl implements NotifyListener {

        private final Logger log = LoggerFactory.getLogger(NotifyListenerImpl.class);

        private final RpcLoadBalanceClientImpl client;

        public NotifyListenerImpl(RpcLoadBalanceClientImpl client) {
            this.client = client;
        }

        @Override
        public void notify(String serviceName, List<InetSocketAddress> addresses) {
            // 创建新的客户端
            addresses.forEach(address -> {
                Map<String, RpcClient> globalClients = client.globalClients;
                globalClients.computeIfAbsent(address.toString(), key -> {
                    RpcClient rpcClient = new RpcClientImpl(address);
                    rpcClientFunctions.forEach(c -> c.accept(rpcClient));
                    log.info("start {} RPC client", rpcClient);
                    startRpcClient(rpcClient);
                    return rpcClient;
                });
            });

            synchronized (client.serviceClients) {
                // 通过地址从全局 client 获取 RPC client
                List<RpcClient> clients = addresses
                        .stream()
                        .map(address -> client.globalClients.get(address.toString()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                List<RpcClient> rpcClients = client.serviceClients.computeIfAbsent(serviceName, n -> new ArrayList<>());
                rpcClients.clear();
                rpcClients.addAll(clients);
            }
        }
    }

    private void startRpcClient(RpcClient client) {
        new Thread(() -> {
            try {
                client.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}
