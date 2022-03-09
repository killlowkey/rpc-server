package com.github.rpc;

import com.github.rpc.core.NettyClientProcessor;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.Future;

/**
 * @author Ray
 * @date created in 2022/3/5 10:28
 */
public interface RpcClient extends InvokeOperation, SslOperation, HealthCheck {

    RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception;

    Future<RpcResponse> sendNoBlockRequest(RpcRequest rpcRequest) throws Exception;

    void addProcessor(NettyClientProcessor processor);

    void start() throws Exception;

    void close();

    boolean isRunning();
}
