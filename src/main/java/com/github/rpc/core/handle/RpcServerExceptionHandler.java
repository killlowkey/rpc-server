package com.github.rpc.core.handle;

import com.github.rpc.core.RpcResponse;
import com.github.rpc.exceptions.RpcServerException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务端异常处理
 *
 * @author Ray
 * @date created in 2022/3/5 21:37
 */
public class RpcServerExceptionHandler extends ChannelDuplexHandler {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        if (cause instanceof RpcServerException) {
            RpcServerException ex = (RpcServerException) cause;
            RpcResponse response = new RpcResponse(ex);
            ctx.writeAndFlush(response);
            return;
        }

        super.exceptionCaught(ctx, cause);
    }

}
