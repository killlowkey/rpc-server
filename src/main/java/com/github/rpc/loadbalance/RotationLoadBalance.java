package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import com.github.rpc.registry.ServiceDiscovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RotationLoadBalance extends AbstractLoadBalance {

    private final AtomicInteger counter = new AtomicInteger();

    public RotationLoadBalance(ServiceDiscovery discovery,
                               Map<String, RpcClient> globalClients,
                               Map<String, List<RpcClient>> serviceClients) {
        super(discovery, globalClients, serviceClients);
    }

    @Override
    public RpcClient doSelect(List<RpcClient> clients) {
        int index = counter.getAndIncrement() % clients.size();
        return clients.get(index);
    }

}
