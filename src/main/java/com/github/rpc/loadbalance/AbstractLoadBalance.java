package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/9 21:17
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    protected final List<RpcClient> rpcClients;

    protected AbstractLoadBalance(List<RpcClient> rpcClients) {
        this.rpcClients = rpcClients;
    }

    @Override
    public RpcClient select() {
        // 移除无效的 client
        List<RpcClient> failedClients = this.rpcClients
                .stream()
                .filter(c -> !c.isRunning())
                .collect(Collectors.toList());
        this.rpcClients.removeAll(failedClients);

        RpcClient rpcClient = doSelect();
        if (Logger.isDebugEnabled()) {
            Logger.debug("{} select {} rpcClient", this.getClass().getSimpleName(), rpcClient);
        }
        return rpcClient;
    }

    public abstract RpcClient doSelect();

}
