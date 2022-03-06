package com.github.rpc.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.RpcClient;
import com.github.rpc.core.handle.RpcRequestCodec;
import com.github.rpc.core.handle.RpcResponseCodec;
import com.github.rpc.core.handle.RpcResponseHandler;
import com.github.rpc.exceptions.ClientInvocationException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tinylog.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2022/3/6 9:55
 */
public class RpcClientImpl implements RpcClient {

    private static final String DEFAULT_VERSION = "2.0";
    private final AtomicLong idCounter = new AtomicLong();
    private final ObjectMapper mapper = new ObjectMapper();
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final ReentrantLock lock = new ReentrantLock();
    // 存放请求
    private final ArrayBlockingQueue<RpcRequest> sendingQueue = new ArrayBlockingQueue<>(64);
    // 存放响应
    private final ArrayBlockingQueue<RpcResponse> responseReceivers = new ArrayBlockingQueue<>(16);
    private Bootstrap bootstrap;
    private Channel channel;
    private boolean isRunning;

    public RpcClientImpl(InetSocketAddress address) {
        this.initBootStrap(address);
    }

    protected RpcClientImpl() {

    }

    private void initBootStrap(InetSocketAddress address) {
        bootstrap = new Bootstrap()
                .group(this.group)
                .remoteAddress(address)
                .channel(NioSocketChannel.class);
    }


    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        try {
            lock.lock();
            // 没有响应直接发送请求
            if (this.responseReceivers.isEmpty()) {
                if (Logger.isDebugEnabled()) {
                    Logger.debug("send request#{} to rpc server", rpcRequest.getId());
                }
                this.channel.writeAndFlush(rpcRequest);
            } else {
                // 等待响应情况，请求需要放入到请求队列
                this.sendingQueue.offer(rpcRequest);
            }
        } finally {
            lock.unlock();
        }

        // 获取响应，阻塞操作
        RpcResponse rpcResponse = this.responseReceivers.take();
        sendNextRequest();
        return rpcResponse;
    }

    private void sendNextRequest() {
        if (!sendingQueue.isEmpty()) {
            RpcRequest rpcRequest = this.sendingQueue.poll();
            if (Logger.isDebugEnabled()) {
                Logger.debug("send request#{} to rpc server", rpcRequest.getId());
            }
            this.channel.writeAndFlush(rpcRequest);
        }
    }

    @Override
    public void start() throws Exception {
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        // 请求和响应编解码器
                        // note：先响应编码器，后请求编码器
                        // 响应编码器的 decode 数据之后，请求编码器的 decode 就不会进行工作
                        // 所以位置一定要正确，否则无法正确编码
                        .addLast(new RpcResponseCodec())
                        .addLast(new RpcRequestCodec())
                        // 响应处理器
                        .addLast(new RpcResponseHandler(responseReceivers));
            }
        });

        // 连接 channel
        ChannelFuture channelFuture = bootstrap.connect().sync();
        this.channel = channelFuture.channel();

        if (Logger.isDebugEnabled()) {
            Logger.debug("rpc client start success, remote server address {}",
                    this.channel.remoteAddress());
        }

        this.isRunning = true;
        // 等待关闭 channel
        this.channel.closeFuture().sync();
    }

    @Override
    public void close() {
        try {
            // 关闭 channel
            this.channel.close().sync();
            // 释放连接池资源
            this.group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public Object invoke(String methodName, Object[] arguments, Type returnType, Map<String, String> extraHeaders) throws Throwable {
        String id = String.valueOf(idCounter.getAndIncrement());
        RpcRequest rpcRequest = new RpcRequest(id, DEFAULT_VERSION, methodName, arguments);

        // 发送请求
        RpcResponse response = sendRequest(rpcRequest);
        resolveResponseError(response);

        if (hasResult(response)) {
            // 无返回值，但是响应中有返回值
            if (isReturnTypeInvalid(returnType)) {
                return null;
            }

            // 构建返回值
            return constructResponseObject(returnType, response.getResult());
        }

        return null;
    }

    private boolean hasResult(RpcResponse rpcResponse) {
        return rpcResponse.getResult() != null;
    }

    private boolean isReturnTypeInvalid(Type returnType) {
        if (returnType == null || returnType == Void.class) {
            Logger.warn("Server returned result but returnType is null");
            return true;
        }
        return false;
    }

    private Object constructResponseObject(Type returnType, Object result) throws IOException {
        JsonNode jsonNode = this.mapper.valueToTree(result);
        JsonParser returnJsonParser = this.mapper.treeAsTokens(jsonNode);
        JavaType returnJavaType = this.mapper.getTypeFactory().constructType(returnType);
        return this.mapper.readValue(returnJsonParser, returnJavaType);
    }

    private void resolveResponseError(RpcResponse rpcResponse) {
        if (rpcResponse.getError() != null) {
            throw new ClientInvocationException(rpcResponse.getError());
        }
    }

    @Override
    public void invoke(String methodName, Object argument) throws Throwable {
        invoke(methodName, new Object[]{argument}, null, new HashMap<>());
    }

    @Override
    public Object invoke(String methodName, Object[] arguments, Type returnType) throws Throwable {
        return invoke(methodName, arguments, returnType, new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(String methodName, Object[] arguments, Class<T> clazz) throws Throwable {
        return (T) invoke(methodName, arguments, Type.class.cast(clazz));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(String methodName, Object[] arguments, Class<T> clazz, Map<String, String> extraHeaders)
            throws Throwable {
        return (T) invoke(methodName, arguments, Type.class.cast(clazz), extraHeaders);
    }

}
