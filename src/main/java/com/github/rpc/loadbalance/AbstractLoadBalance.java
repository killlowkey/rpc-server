package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import com.github.rpc.registry.RegisterMode;
import com.github.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/9 21:17
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractLoadBalance.class);

    protected final Map<String, RpcClient> globalClients;
    protected final Map<String, List<RpcClient>> serviceClients;
    protected final ServiceDiscovery discovery;

    protected AbstractLoadBalance(ServiceDiscovery discovery,
                                  Map<String, RpcClient> globalClients,
                                  Map<String, List<RpcClient>> serviceClients) {
        this.discovery = discovery;
        this.globalClients = globalClients;
        this.serviceClients = serviceClients;
    }

    @Override
    public RpcClient select(String serviceName) {
        List<RpcClient> rpcClients;
        synchronized (serviceClients) {
            rpcClients = this.serviceClients
                    .computeIfAbsent(serviceName, name -> new ArrayList<>());
        }

        if (discovery.getMode() == RegisterMode.MEMORY) {
            removeInvalidClient(rpcClients);
        }

        if (rpcClients.isEmpty()) {
            throw new IllegalStateException("none available rpc clients");
        }

        RpcClient rpcClient = doSelect(rpcClients);
        if (logger.isDebugEnabled()) {
            logger.debug("{} select {} rpcClient", this.getClass().getSimpleName(), rpcClient);
        }
        return rpcClient;
    }

    private void removeInvalidClient(List<RpcClient> rpcClients) {
        // 移除无效的 client
        List<RpcClient> failedClients = rpcClients
                .stream()
                // client 不在运行，又不处于链接状态
                .filter(client -> !client.isRunnable() && !client.isConnecting())
                .collect(Collectors.toList());
        rpcClients.removeAll(failedClients);

        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, RpcClient> entry : globalClients.entrySet()) {
            if (failedClients.contains(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        // 移除无效 RpcClient
        keys.forEach(globalClients::remove);
    }

    public abstract RpcClient doSelect(List<RpcClient> clients);

}
