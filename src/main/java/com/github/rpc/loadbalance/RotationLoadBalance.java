package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RotationLoadBalance implements LoadBalance {

    private final List<RpcClient> rpcClients;
    private final AtomicInteger counter = new AtomicInteger();

    public RotationLoadBalance(List<RpcClient> rpcClients) {
        this.rpcClients = rpcClients;
    }

    @Override
    public RpcClient select() {
        int index = counter.getAndIncrement() % rpcClients.size();
        RpcClient rpcClient = this.rpcClients.get(index);
        if (Logger.isDebugEnabled()) {
            Logger.debug("RandomLoadBalance select {} rpcClient", rpcClient);
        }
        return rpcClient;
    }

}
