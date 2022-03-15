package com.github.rpc.core;

/**
 * rpc 服务监听器
 *
 * @author Ray
 * @date created in 2022/3/15 22:21
 */
public interface RpcServerListener {

    void onStartCompleted();

    void onStopCompleted();

}
