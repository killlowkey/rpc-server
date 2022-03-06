package com.github.rpc.exceptions;

import com.github.rpc.core.RpcResponse;

/**
 * @author Ray
 * @date created in 2022/3/6 10:14
 */
public class ClientInvocationException extends RuntimeException {
    private final long code;

    public ClientInvocationException(long code, String msg) {
        super(msg);
        this.code = code;
    }

    public ClientInvocationException(RpcResponse.ErrorMsg errorMsg) {
        this(errorMsg.getCode(), errorMsg.getMessage());
    }

    public long getCode() {
        return this.code;
    }
}
