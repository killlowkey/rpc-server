package com.github.rpc;

import com.github.rpc.core.RpcResponse;
import io.netty.util.Timeout;

/**
 * @author Ray
 * @date created in 2022/5/29 10:24
 */
public interface InvokeFuture {

    /**
     * 根据超时时间等待响应
     */
    RpcResponse waitResponse(final long timeoutMillis) throws InterruptedException;

    /**
     * 无限期等待响应
     */
    RpcResponse waitResponse() throws InterruptedException;

    /**
     * 查看 future 是否完成
     */
    boolean isDone();

    /**
     * 添加 future 超时
     */
    void addTimeout(Timeout timeout);

    /**
     * 取消超时
     */
    void cancelTimeout();

    /**
     * 执行回调
     */
    void executeInvokeCallback();

    /**
     * 请求调用 id
     */
    int invokeId();

    /**
     * 设置响应
     */
    void setResponse(RpcResponse response);

    void setCause(Throwable cause);

    Throwable getCause();
}
