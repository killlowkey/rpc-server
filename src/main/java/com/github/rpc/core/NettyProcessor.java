package com.github.rpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

/**
 * @author Ray
 * @date created in 2022/3/7 8:28
 */
public interface NettyProcessor {

    default void processServerBootstrap(ServerBootstrap bootstrap) {

    }

    default void processBootstrap(Bootstrap bootstrap) {

    }

    default void processChannel(Channel channel) {

    }

}
