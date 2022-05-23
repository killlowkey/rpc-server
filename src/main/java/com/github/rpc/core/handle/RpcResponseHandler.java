package com.github.rpc.core.handle;

import com.github.rpc.core.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;
import java.util.Queue;

/**
 * Rpc 响应处理器
 *
 * @author Ray
 * @date created in 2022/3/5 12:12
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RpcResponseHandler.class);

    private final Queue<RpcResponse> responseReceivers;

    public RpcResponseHandler(Queue<RpcResponse> responseReceivers) {
        this.responseReceivers = responseReceivers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("offer request#{} response to responseReceivers queue", response.getId());
        }

        // 将响应放入到响应队列中
        this.responseReceivers.offer(response);
    }

}
