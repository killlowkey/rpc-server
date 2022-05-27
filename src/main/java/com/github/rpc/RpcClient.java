package com.github.rpc;

import com.github.rpc.core.NettyProcessor;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import com.github.rpc.serializer.Serializer;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * @author Ray
 * @date created in 2022/3/5 10:28
 */
public interface RpcClient extends InvokeOperation, SslOperation, HealthCheck {

    RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception;

    Future<RpcResponse> sendNoBlockRequest(RpcRequest rpcRequest) throws Exception;

    void setSerializer(Serializer serializer);

    void addProcessor(NettyProcessor processor);

    void start() throws Exception;

    void close();

    boolean isRunnable();

    boolean isConnecting();

    InetSocketAddress getRemoteAddress();
}
