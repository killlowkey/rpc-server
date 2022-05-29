package com.github.rpc.core.handle;

import com.github.rpc.InvokeFuture;
import com.github.rpc.core.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rpc 响应处理器
 *
 * @author Ray
 * @date created in 2022/3/5 12:12
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RpcResponseHandler.class);
    public static final AttributeKey<Map<Integer, InvokeFuture>> FUTURE = AttributeKey.newInstance("future");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel()
                .attr(FUTURE)
                .compareAndSet(null, new ConcurrentHashMap<>());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.channel()
                .attr(FUTURE)
                .set(null);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("handle id={} RpcResponse", response.getId());
        }

        Map<Integer, InvokeFuture> invokeFutureMap = ctx.channel().attr(FUTURE).get();
        if (invokeFutureMap == null || invokeFutureMap.isEmpty()) {
            return;
        }

        int invokeId = Integer.parseInt(response.getId());
        InvokeFuture invokeFuture = invokeFutureMap.get(invokeId);
        if (invokeFuture != null) {
            try {
                invokeFutureMap.remove(invokeId);
                invokeFuture.setResponse(response);
                // 取消超时
                invokeFuture.cancelTimeout();
            } catch (Exception ex) {
                invokeFuture.setCause(ex);
            } finally {
                // 执行回调
                invokeFuture.executeInvokeCallback();
            }
        }
    }

}
