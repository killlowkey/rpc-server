package com.github.rpc.constant;

import lombok.Getter;

/**
 * @author Ray
 * @date created in 2022/3/5 21:28
 */
@Getter
public enum ErrorEnum {

    PARSE_ERROR(-32700, "parse json error"),
    INVALID_REQUEST(32600, "invalid request"),
    METHOD_NOT_FOUND(-32601, "method not found"),
    INVALID_PARAMS(-32602, "invalid params"),
    INTERNAL_ERROR(-32603, "internal_error"),
    ADAPT_METHOD_PARAM_ERROR(-32000, "adapt method param error"),
    TRIGGER_RATE_LIMIT(-32001, "trigger rate limit");

    private final int code;
    private final String msg;

    ErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
