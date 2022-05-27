package com.github.rpc.loadbalance;

import ch.qos.logback.core.net.server.Client;
import com.github.rpc.RpcClient;
import com.github.rpc.registry.ServiceDiscovery;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 随机负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    private final Random random = new Random();

    public RandomLoadBalance(ServiceDiscovery discovery,
                             Map<String, RpcClient> globalClients,
                             Map<String, List<RpcClient>> serviceClients) {
        super(discovery, globalClients, serviceClients);
    }

    @Override
    public RpcClient doSelect(List<RpcClient> clients) {
        int index = random.nextInt(100) % clients.size();
        return clients.get(index);
    }

}
