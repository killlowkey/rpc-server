package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.loadbalance.LoadBalance;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import com.github.rpc.loadbalance.RandomLoadBalance;
import com.github.rpc.loadbalance.RotationLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/6 13:51
 */
public class RpcLoadBalanceClientImpl extends RpcClientImpl {

    private LoadBalance loadBalance;

    public RpcLoadBalanceClientImpl(List<InetSocketAddress> address, LoadBalanceStrategy strategy) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }

        this.initLoadBalance(address, strategy);
    }

    private void initLoadBalance(List<InetSocketAddress> address, LoadBalanceStrategy strategy) {
        List<RpcClient> rpcClients = address.stream()
                .map(RpcClientImpl::new)
                .collect(Collectors.toList());

        rpcClients.forEach(rpcClient -> new Thread(() -> {
            try {
                rpcClient.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start());

        try {
            // 等待所有客户端连接
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (strategy == LoadBalanceStrategy.RANDOM) {
            this.loadBalance = new RandomLoadBalance(rpcClients);
        } else if (strategy == LoadBalanceStrategy.ROTATION) {
            this.loadBalance = new RotationLoadBalance(rpcClients);
        } else {
            throw new IllegalStateException("LoadBalanceStrategy is unknown");
        }


    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        RpcClient rpcClient = this.loadBalance.select();
        return rpcClient.sendRequest(rpcRequest);
    }
}
