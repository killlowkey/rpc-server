package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import org.tinylog.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 随机负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:38
 */
public class RandomLoadBalance implements LoadBalance {

    private final List<RpcClient> rpcClients;
    private final Random random = new Random();

    public RandomLoadBalance(List<RpcClient> rpcClients) {
        this.rpcClients = rpcClients;
    }

    @Override
    public RpcClient select() {
        int index = random.nextInt(100) % rpcClients.size();
        RpcClient rpcClient = this.rpcClients.get(index);
        if (Logger.isDebugEnabled()) {
            Logger.debug("RandomLoadBalance select {} rpcClient", rpcClient);
        }
        return rpcClient;
    }
}
