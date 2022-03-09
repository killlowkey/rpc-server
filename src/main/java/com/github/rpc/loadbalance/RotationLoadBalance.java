package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RotationLoadBalance extends AbstractLoadBalance {

    private final AtomicInteger counter = new AtomicInteger();

    public RotationLoadBalance(List<RpcClient> rpcClients) {
        super(rpcClients);
    }

    @Override
    public RpcClient doSelect() {
        int index = counter.getAndIncrement() % rpcClients.size();
        return this.rpcClients.get(index);
    }

}
