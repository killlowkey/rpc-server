package com.github.rpc.core;

import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.core.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 底层响应实现
 *
 * @author Ray
 * @date created in 2022/5/23 21:07
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DefaultRpcResponse implements RpcResponse {
    private String id;
    private int code;
    private String message;
    private Object result;
    public DefaultRpcResponse(String id) {
        this.id = id;
    }

    public void setError(ErrorEnum errorEnum) {
        if (errorEnum == null) {
            return;
        }
        this.code = errorEnum.getCode();
        this.message = errorEnum.getMsg();
    }

    public void success() {
        this.code = 200;
        this.message = "success";
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Object getResult() {
        return this.result;
    }

}
