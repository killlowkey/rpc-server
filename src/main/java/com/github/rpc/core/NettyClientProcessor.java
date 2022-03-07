package com.github.rpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * @author Ray
 * @date created in 2022/3/7 8:52
 */
public interface NettyClientProcessor {

    default void process(Bootstrap bootstrap) {

    }

    default void processChannel(Channel channel) {

    }

}
