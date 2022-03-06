package com.github.rpc.core.handle;

import com.github.rpc.core.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

import java.util.Queue;

/**
 * Rpc 响应处理器
 *
 * @author Ray
 * @date created in 2022/3/5 12:12
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final Queue<RpcResponse> responseReceivers;

    public RpcResponseHandler(Queue<RpcResponse> responseReceivers) {
        this.responseReceivers = responseReceivers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (Logger.isDebugEnabled()) {
            Logger.debug("offer request#{} response to responseReceivers queue", response.getId());
        }

        // 将响应放入到响应队列中
        this.responseReceivers.offer(response);
    }

}
