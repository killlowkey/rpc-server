package com.github.rpc.core.handle;

import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.core.DefaultRpcResponse;
import com.github.rpc.core.JsonParamAdapter;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.exceptions.MethodNotFoundException;
import com.github.rpc.exceptions.RpcServerException;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.registry.Entry;
import com.github.rpc.registry.Registry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Ray
 * @date created in 2022/3/5 13:05
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private final MethodInvokeDispatcher dispatcher;
    private final Registry registry;
    private final JsonParamAdapter jsonParamAdapter = new JsonParamAdapter();
    private RpcRequest currentRequest;

    public RpcRequestHandler(MethodInvokeDispatcher dispatcher,
                             Registry registry) {
        this.dispatcher = dispatcher;
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // 设置请求
        this.currentRequest = rpcRequest;

        if (logger.isDebugEnabled()) {
            logger.debug("handle [{}] request, id={}", rpcRequest.getName(), rpcRequest.getId());
        }

        Method method = findMethod(rpcRequest.getName());
        // rpc 服务没有该方法
        if (method == null) {
            DefaultRpcResponse response = new DefaultRpcResponse(currentRequest.getId());
            response.setError(ErrorEnum.METHOD_NOT_FOUND);
            ctx.writeAndFlush(response);
            currentRequest = null;
            return;
        }

        try {
            // 适配方法参数
            Object[] params = jsonParamAdapter.adapt(method, rpcRequest.getParams());
            // 调用方法：com.github.PersonService#say
            DefaultRpcResponse response = invoke(rpcRequest.getName(), params, rpcRequest.getId());
            // 成功
            response.success();
            ctx.writeAndFlush(response);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            currentRequest = null;
        }
    }

    private DefaultRpcResponse invoke(String name, Object[] params, String id) throws Throwable {
        DefaultRpcResponse response = new DefaultRpcResponse(id);
        // 调用方法并获取结果
        Object result = this.dispatcher.invoke(name, params);
        response.setResult(result);
        return response;
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String id = currentRequest == null ? "" : currentRequest.getId();
        DefaultRpcResponse response = new DefaultRpcResponse(id);

        if (cause instanceof MethodNotFoundException) {
            response.setError(ErrorEnum.METHOD_NOT_FOUND);
        } else if (cause instanceof RpcServerException) {
            response.setCode(500);
            response.setMessage(cause.getMessage());
        } else {
            response.setCode(500);
            response.setMessage(cause.getMessage());
        }

        try {
            ctx.writeAndFlush(response);
        } finally {
            currentRequest = null;
        }
    }

    private Method findMethod(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return null;
        }

        String[] names = name.split("#");
        if (names.length != 2) {
            return null;
        }

        Entry entry = registry.lookupEntry(names[0], names[1]);
        return entry == null ? null : entry.getMethod();
    }
}
