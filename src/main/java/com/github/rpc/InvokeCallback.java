package com.github.rpc;

import com.github.rpc.core.RpcResponse;

/**
 * 调用回调
 *
 * @author Ray
 * @date created in 2022/5/29 10:37
 */
public interface InvokeCallback {

    void onResponse(RpcResponse response);

    void onException(Throwable cause);

}
