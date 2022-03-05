package com.github.rpc.exceptions;

import com.github.rpc.constant.ErrorEnum;

/**
 * @author Ray
 * @date created in 2022/3/5 21:33
 */
public class RpcServerException extends RuntimeException {
    private final long code;
    private final String id;

    public RpcServerException(String id, long code, String msg) {
        super(msg);
        this.code = code;
        this.id = id;
    }

    public RpcServerException(long code, String msg) {
        this("", code, msg);
    }

    public RpcServerException(ErrorEnum errorEnum) {
        this(errorEnum, "");
    }

    public RpcServerException(ErrorEnum errorEnum, String id) {
        this(id, errorEnum.getCode(), errorEnum.getMsg());
    }

    public long getCode() {
        return this.code;
    }

    public String getId() {
        return this.id;
    }
}
