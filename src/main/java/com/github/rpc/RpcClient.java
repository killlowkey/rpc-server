package com.github.rpc;

import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;

/**
 * @author Ray
 * @date created in 2022/3/5 10:28
 */
public interface RpcClient extends InvokeOperation {

    RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception;

    void start() throws Exception;

    void close();

    boolean isRunning();
}
