package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.loadbalance.LoadBalance;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import com.github.rpc.loadbalance.RandomLoadBalance;
import com.github.rpc.loadbalance.RotationLoadBalance;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/6 13:51
 */
public class RpcLoadBalanceClientImpl extends RpcClientImpl {

    private LoadBalance loadBalance;
    private List<RpcClient> rpcClients;

    public RpcLoadBalanceClientImpl(List<InetSocketAddress> address, LoadBalanceStrategy strategy) {
        if (address == null) {
            throw new IllegalArgumentException("address cannot be null");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }

        this.init(address, strategy);
    }

    @Override
    public void enableSsl(File jksFile, String password, boolean needClientAuth) {
        this.rpcClients.forEach(rpcClient -> rpcClient.enableSsl(jksFile, password, false));
    }


    private void init(List<InetSocketAddress> address, LoadBalanceStrategy strategy) {
        // 初始化所有客户端
        this.rpcClients = address.stream()
                .map(RpcClientImpl::new)
                .collect(Collectors.toList());

        // 初始化负载均衡
        if (strategy == LoadBalanceStrategy.RANDOM) {
            this.loadBalance = new RandomLoadBalance(rpcClients);
        } else if (strategy == LoadBalanceStrategy.ROTATION) {
            this.loadBalance = new RotationLoadBalance(rpcClients);
        } else {
            throw new IllegalStateException("LoadBalanceStrategy is unknown");
        }

    }

    @Override
    public void start() throws Exception {
        // 启动所有客户端
        this.rpcClients.forEach(rpcClient -> {
            new Thread(() -> {
                try {
                    rpcClient.start();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }).start();
        });
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        // 调用负载均衡选择 rpc client
        RpcClient rpcClient = this.loadBalance.select();
        return rpcClient.sendRequest(rpcRequest);
    }

    @Override
    public Future<RpcResponse> sendNoBlockRequest(RpcRequest rpcRequest) throws Exception {
        RpcClient rpcClient = this.loadBalance.select();
        return rpcClient.sendNoBlockRequest(rpcRequest);
    }

    @Override
    public boolean isRunning() {
        // 遍历所有的 rpc client 是否启动
        for (RpcClient rpcClient : this.rpcClients) {
            if (!rpcClient.isRunning()) {
                return false;
            }
        }

        return true;
    }
}
