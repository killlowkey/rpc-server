package com.github.rpc;

import com.github.rpc.core.NettyServerProcessor;
import com.github.rpc.core.RpcServerListener;

/**
 * @author Ray
 * @date created in 2022/3/3 7:52
 */
public interface RpcServer extends SslOperation {

    void start();

    void stop();

    void addListener(RpcServerListener listener);

    boolean isRunnable();

    void addProcessor(NettyServerProcessor processor);

}
