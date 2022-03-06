package com.github.rpc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.exceptions.RpcServerException;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rpc 响应
 *
 * @author Ray
 * @date created in 2022/3/5 9:40
 */
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RpcResponse {

    private String version = "2.0";
    private String id;
    private ErrorMsg error;
    private Object result;

    public RpcResponse(ErrorMsg error) {
        this.error = error;
    }

    public RpcResponse(ErrorEnum errorEnum) {
        this(new ErrorMsg(errorEnum.getCode(), errorEnum.getMsg()));
    }

    public RpcResponse(RpcServerException ex) {
        this.error = new ErrorMsg(ex.getCode(), ex.getMessage());
    }

    public RpcResponse(String id) {
        this.id = id;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorMsg {
        private long code;
        private String message;
        private Object data;

        public ErrorMsg(long code, String message) {
            this.code = code;
            this.message = message;
        }

        public ErrorMsg(ErrorEnum errorEnum) {
            this.code = errorEnum.getCode();
            this.message = errorEnum.getMsg();
        }
    }

}
