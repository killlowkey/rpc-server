package com.github.rpc;

/**
 * @author Ray
 * @date created in 2022/3/3 7:52
 */
public interface RpcServer {

    void start();

    void stop();

    void addListener();

}
