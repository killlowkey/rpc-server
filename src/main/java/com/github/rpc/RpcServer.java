package com.github.rpc;

import com.github.rpc.core.NettyServerProcessor;

/**
 * @author Ray
 * @date created in 2022/3/3 7:52
 */
public interface RpcServer extends SslOperation {

    void start();

    void stop();

    void addListener();

    void addProcessor(NettyServerProcessor processor);

}
