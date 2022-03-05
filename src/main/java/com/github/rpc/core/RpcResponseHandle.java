package com.github.rpc.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Queue;

/**
 * Rpc 响应处理器
 *
 * @author Ray
 * @date created in 2022/3/5 12:12
 */
public class RpcResponseHandle extends SimpleChannelInboundHandler<RpcResponse> {

    private final Queue<RpcResponse> responseReceivers;

    public RpcResponseHandle(Queue<RpcResponse> responseReceivers) {
        this.responseReceivers = responseReceivers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 将响应放入到响应队列中
        this.responseReceivers.offer(response);
    }

}
