package com.github.rpc.core.handle;

import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.core.JsonParamAdapter;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import com.github.rpc.exceptions.MethodNotFoundException;
import com.github.rpc.exceptions.RpcServerException;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.utils.RpcUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ray
 * @date created in 2022/3/5 13:05
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);

    private final MethodInvokeDispatcher dispatcher;
    private final JsonParamAdapter jsonParamAdapter = new JsonParamAdapter();

    public RpcRequestHandler(MethodInvokeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        String name = rpcRequest.getName();

        // rpc 服务没有该方法
        if (StringUtil.isNullOrEmpty(name) || RpcUtil.getMethod(name) == null) {
            writeAndFlush(ErrorEnum.METHOD_NOT_FOUND, rpcRequest, ctx);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("handle [{}] rpc request", rpcRequest.getName());
        }

        try {
            // 适配方法参数
            Object[] params = jsonParamAdapter.adapt(RpcUtil.getMethod(name), rpcRequest.getParams());
            // 调用方法
            RpcResponse response = invoke(name, params, rpcRequest.getId());
            ctx.writeAndFlush(response);
        } catch (Throwable ex) {
            writeAndFlush(ex, rpcRequest, ctx);
        }

    }

    private RpcResponse invoke(String name, Object[] params, String id) throws Throwable {
        RpcResponse response = new RpcResponse(id);
        // 调用方法并获取结果
        Object result = this.dispatcher.invoke(name, params);
        response.setResult(result);
        return response;
    }

    private void writeAndFlush(Throwable ex, RpcRequest rpcRequest, ChannelHandlerContext ctx) {
        RpcResponse response = new RpcResponse(rpcRequest.getId());
        if (ex instanceof MethodNotFoundException) {
            RpcResponse.ErrorMsg msg = new RpcResponse.ErrorMsg(ErrorEnum.METHOD_NOT_FOUND);
            response.setError(msg);
        } else if (ex instanceof RpcServerException) {
            response = new RpcResponse((RpcServerException) ex);
            response.setId(rpcRequest.getId());
        } else {
            response = new RpcResponse(rpcRequest.getId());
            RpcResponse.ErrorMsg msg = new RpcResponse.ErrorMsg(-32603, ex.getMessage());
            response.setError(msg);
        }

        ctx.writeAndFlush(response);
    }

    private void writeAndFlush(ErrorEnum errorEnum, RpcRequest rpcRequest, ChannelHandlerContext ctx) {
        RpcResponse response = new RpcResponse(errorEnum);
        response.setId(rpcRequest.getId());
        ctx.writeAndFlush(response);
    }

}
