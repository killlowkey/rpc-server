package com.github.rpc.core;

import com.github.rpc.exceptions.MethodNotFoundException;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.utils.RpcUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.tinylog.Logger;

import java.io.IOException;

/**
 * @author Ray
 * @date created in 2022/3/5 13:05
 */
public class RpcRequestHandle extends SimpleChannelInboundHandler<RpcRequest> {

    private final MethodInvokeDispatcher dispatcher;
    private final JsonParamAdapter jsonParamAdapter = new JsonParamAdapter();

    public RpcRequestHandle(MethodInvokeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        String name = rpcRequest.getName();

        // rpc 服务没有该方法
        if (StringUtil.isNullOrEmpty(name) || RpcUtil.getMethod(name) == null) {
            RpcResponse response = new RpcResponse();
            RpcResponse.ErrorMsg errorMsg = new RpcResponse.ErrorMsg(-32601, "Method not found");
            response.setError(errorMsg);
            ctx.writeAndFlush(response);
            return;
        }

        if (Logger.isDebugEnabled()) {
            Logger.debug("handle [{}] rpc request", rpcRequest.getName());
        }

        Object[] params = jsonParamAdapter.adapt(RpcUtil.getMethod(name), rpcRequest.getParams());
        try {
            // 适配方法参数
            params = jsonParamAdapter.adapt(RpcUtil.getMethod(name), rpcRequest.getParams());
        } catch (IOException ex) {
            RpcResponse response = new RpcResponse();
            RpcResponse.ErrorMsg errorMsg = new RpcResponse.ErrorMsg(-32000, ex.getMessage());
            response.setError(errorMsg);
            ctx.writeAndFlush(response);
        }

        // 调用方法
        RpcResponse response = invoke(name, params);
        ctx.writeAndFlush(response);
    }

    private RpcResponse invoke(String name, Object[] params) {
        RpcResponse response = new RpcResponse();
        try {
            // 调用方法并获取结果
            Object result = this.dispatcher.invoke(name, params);
            response.setResult(result);
        } catch (MethodNotFoundException throwable) {
            RpcResponse.ErrorMsg errorMsg = new RpcResponse.ErrorMsg(-32601, "Method not found");
            response.setError(errorMsg);
        } catch (Throwable throwable) {
            RpcResponse.ErrorMsg errorMsg = new RpcResponse.ErrorMsg(-32600, throwable.getMessage());
            response.setError(errorMsg);
        }
        return response;
    }
}
