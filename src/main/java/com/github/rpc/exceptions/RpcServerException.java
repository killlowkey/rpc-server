package com.github.rpc.exceptions;

import com.github.rpc.constant.ErrorEnum;

/**
 * @author Ray
 * @date created in 2022/3/5 21:33
 */
public class RpcServerException extends RuntimeException {

    private final long code;

    public RpcServerException(long code, String msg) {
        super(msg);
        this.code = code;
    }

    public RpcServerException(ErrorEnum errorEnum) {
        this(errorEnum.getCode(), errorEnum.getMsg());
    }

    public long getCode() {
        return this.code;
    }

}
