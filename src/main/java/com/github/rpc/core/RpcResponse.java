package com.github.rpc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ray
 * @date created in 2022/3/5 9:40
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RpcResponse {

    private String version;
    private String id;
    private ErrorMsg error;
    private Object result;

    public RpcResponse(ErrorMsg error) {
        this.error = error;
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
    }

}
