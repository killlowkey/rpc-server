package com.github.rpc.core;

/**
 * RPC 请求定义
 *
 * @author Ray
 * @date created in 2022/5/23 20:01
 */
public interface RpcRequest {
    /**
     * 请求 id
     */
    String getId();

    /**
     * 请求方法
     */
    String getName();

    /**
     * 请求参数
     */
    Object[] getParams();

}
