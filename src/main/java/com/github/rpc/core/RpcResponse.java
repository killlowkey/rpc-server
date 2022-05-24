package com.github.rpc.core;

/**
 * RPC 请求定义
 *
 * @author Ray
 * @date created in 2022/5/23 20:19
 */
public interface RpcResponse {
    /**
     * 响应 id
     */
    String getId();

    /**
     * 响应码
     */
    int getCode();

    /**
     * 响应信息
     */
    String getMessage();

    /**
     * 调用结果
     */
    Object getResult();

}
