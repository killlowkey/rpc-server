package com.github.rpc;

import com.github.rpc.core.NettyProcessor;
import com.github.rpc.core.RpcServerListener;
import com.github.rpc.registry.Registry;

/**
 * @author Ray
 * @date created in 2022/3/3 7:52
 */
public interface RpcServer extends SslOperation {

    void start();

    void stop();

    void addListener(RpcServerListener listener);

    boolean isRunnable();

    void addProcessor(NettyProcessor processor);

    void setRegistry(Registry registry);

}
