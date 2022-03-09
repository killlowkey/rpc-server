package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    private final Random random = new Random();

    public RandomLoadBalance(List<RpcClient> rpcClients) {
        super(rpcClients);
    }

    @Override
    public RpcClient doSelect() {
        int index = random.nextInt(100) % rpcClients.size();
        return this.rpcClients.get(index);
    }

}
