package com.github.rpc.core;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.github.rpc.InvokeCallback;
import com.github.rpc.InvokeFuture;
import com.github.rpc.RpcClient;
import com.github.rpc.loadbalance.LoadBalance;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import com.github.rpc.loadbalance.RandomLoadBalance;
import com.github.rpc.loadbalance.RotationLoadBalance;
import com.github.rpc.registry.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Ray
 * @date created in 2022/3/6 13:51
 */
public class RpcLoadBalanceClientImpl extends RpcClientImpl {

    protected final Map<String, RpcClient> globalClients = new ConcurrentHashMap<>();
    protected final Map<String, List<RpcClient>> serviceClients = new ConcurrentHashMap<>();
    private final LoadBalance loadBalance;


    public RpcLoadBalanceClientImpl(ServiceDiscovery discovery,
                                    LoadBalanceStrategy strategy) {
        // 初始化负载均衡
        if (strategy == LoadBalanceStrategy.RANDOM) {
            this.loadBalance = new RandomLoadBalance(discovery, globalClients, serviceClients);
        } else if (strategy == LoadBalanceStrategy.ROTATION) {
            this.loadBalance = new RotationLoadBalance(discovery, globalClients, serviceClients);
        } else {
            throw new IllegalStateException("LoadBalanceStrategy is unknown");
        }
    }

    @Override
    public void enableSsl(File jksFile, String password, boolean needClientAuth) {
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        // 调用负载均衡选择 rpc client
        RpcClient rpcClient = this.loadBalance.select(getServiceName(rpcRequest));
        return rpcClient.sendRequest(rpcRequest);
    }

    @Override
    public InvokeFuture sendRequestWithFuture(RpcRequest rpcRequest,
                                              long timeoutMillis,
                                              InvokeCallback callback) throws Exception {
        RpcClientImpl rpcClient = (RpcClientImpl)this.loadBalance.select(getServiceName(rpcRequest));
        return rpcClient.sendRequestWithFuture(rpcRequest, timeoutMillis, callback);
    }

    private String getServiceName(RpcRequest rpcRequest) {
        return rpcRequest.getName().split("#")[0];
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public void run() {
        // Noop
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        throw new UnsupportedOperationException("not support get remote address");
    }
}
