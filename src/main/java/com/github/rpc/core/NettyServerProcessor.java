package com.github.rpc.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

/**
 * @author Ray
 * @date created in 2022/3/7 8:28
 */
public interface NettyServerProcessor {

    default void process(ServerBootstrap bootstrap) {

    }

    default void processChannel(Channel channel) {

    }

}
