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
import org.tinylog.Logger;

import java.io.IOException;

/**
 * @author Ray
 * @date created in 2022/3/5 13:05
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

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
            throw new RpcServerException(ErrorEnum.METHOD_NOT_FOUND, rpcRequest.getId());
        }

        if (Logger.isDebugEnabled()) {
            Logger.debug("handle [{}] rpc request", rpcRequest.getName());
        }

        Object[] params;
        try {
            // 适配方法参数
            params = jsonParamAdapter.adapt(RpcUtil.getMethod(name), rpcRequest.getParams());
        } catch (IOException ex) {
            throw new RpcServerException(ErrorEnum.ADAPT_METHOD_PARAM_ERROR, rpcRequest.getId());
        }

        // 调用方法
        RpcResponse response = invoke(name, params, rpcRequest.getId());
        ctx.writeAndFlush(response);
    }

    private RpcResponse invoke(String name, Object[] params, String id) {
        RpcResponse response = new RpcResponse(id);
        try {
            // 调用方法并获取结果
            Object result = this.dispatcher.invoke(name, params);
            response.setResult(result);
        } catch (MethodNotFoundException throwable) {
            throw new RpcServerException(ErrorEnum.METHOD_NOT_FOUND, id);
        } catch (Throwable throwable) {
            throw new RpcServerException(ErrorEnum.INTERNAL_ERROR, id);
        }

        return response;
    }
}
