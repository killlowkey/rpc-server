package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/9 21:17
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractLoadBalance.class);

    protected final List<RpcClient> rpcClients;

    protected AbstractLoadBalance(List<RpcClient> rpcClients) {
        this.rpcClients = rpcClients;
    }

    @Override
    public RpcClient select() {
        // 移除无效的 client
        List<RpcClient> failedClients = this.rpcClients
                .stream()
                .filter(c -> c.isRunnable())
                .collect(Collectors.toList());
        this.rpcClients.removeAll(failedClients);

        if (this.rpcClients.isEmpty()) {
            throw new IllegalStateException("none available rpc clients");
        }

        RpcClient rpcClient = doSelect();
        if (logger.isDebugEnabled()) {
            logger.debug("{} select {} rpcClient", this.getClass().getSimpleName(), rpcClient);
        }
        return rpcClient;
    }

    public abstract RpcClient doSelect();

}
