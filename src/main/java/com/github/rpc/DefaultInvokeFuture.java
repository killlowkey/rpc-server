package com.github.rpc;

import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import io.netty.util.Timeout;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Ray
 * @date created in 2022/5/29 10:32
 */
public class DefaultInvokeFuture implements InvokeFuture {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final RpcRequest rpcRequest;
    private final InvokeCallback callback;
    private RpcResponse response;
    private Throwable cause;
    private Timeout timeout;

    public DefaultInvokeFuture(RpcRequest rpcRequest, InvokeCallback callback) {
        Objects.requireNonNull(rpcRequest, "RpcRequest must not null");
        this.rpcRequest = rpcRequest;
        this.callback = callback;
    }

    @Override
    public RpcResponse waitResponse(long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }

    @Override
    public RpcResponse waitResponse() throws InterruptedException {
        this.countDownLatch.await();
        return this.response;
    }

    @Override
    public boolean isDone() {
        return this.countDownLatch.getCount() <= 0;
    }

    @Override
    public void addTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    @Override
    public void cancelTimeout() {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
    }

    @Override
    public void executeInvokeCallback() {
        if (this.callback == null) {
            return;
        }

        if (this.response != null) {
            this.callback.onResponse(response);
        }

        if (this.cause != null) {
            this.callback.onException(this.cause);
        }
    }

    @Override
    public int invokeId() {
        return Integer.parseInt(rpcRequest.getId());
    }

    @Override
    public void setResponse(RpcResponse response) {
        this.response = response;
        this.countDownLatch.countDown();
    }

    @Override
    public void setCause(Throwable cause) {
        this.cause = cause;
        this.countDownLatch.countDown();
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

}
