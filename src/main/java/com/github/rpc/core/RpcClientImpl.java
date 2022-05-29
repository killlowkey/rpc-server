package com.github.rpc.core;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.DefaultInvokeFuture;
import com.github.rpc.InvokeCallback;
import com.github.rpc.InvokeFuture;
import com.github.rpc.RpcClient;
import com.github.rpc.core.handle.RpcResponseHandler;
import com.github.rpc.exceptions.ClientInvocationException;
import com.github.rpc.serializer.Serializer;
import com.github.rpc.serializer.SerializeProcessor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2022/3/6 9:55
 */
public class RpcClientImpl implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientImpl.class);
    public static final int DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_HEALTH_CHECK_INTERVAL = 30;
    private static final int START_TIMEOUT = 5000;
    private final AtomicLong idCounter = new AtomicLong();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Timer timer = new HashedWheelTimer(new NamedThreadFactory("DefaultTimer10", true), 10,
            TimeUnit.MILLISECONDS);
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final List<NettyProcessor> processors = new ArrayList<>();

    private Serializer serializer;

    private Bootstrap bootstrap;
    private Channel channel;
    private boolean isRunning;
    private Date lastHealthCheckDate;
    private int healthCheckFailureCount;
    private InetSocketAddress address;
    private boolean connecting;

    public RpcClientImpl(InetSocketAddress address) {
        this.initBootStrap(address);
        this.initHealthCheck();
    }

    protected RpcClientImpl() {

    }

    private void initBootStrap(InetSocketAddress address) {
        this.address = address;
        bootstrap = new Bootstrap()
                .group(this.group)
                .remoteAddress(address)
                .channel(NioSocketChannel.class);
    }

    private void initHealthCheck() {
        ScheduledExecutorService scheduledService =
                Executors.newSingleThreadScheduledExecutor(new HealthCheckThreadFactory());
        scheduledService.scheduleWithFixedDelay(this, 0, DEFAULT_HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        ensureOpen();

        InvokeFuture invokeFuture = sendRequestWithFuture(rpcRequest);
        // 等待响应
        RpcResponse rpcResponse = invokeFuture.waitResponse(DEFAULT_TIMEOUT);
        if (rpcResponse != null) {
            // 更新健康检查信息
            updateHealthCheckInfo();
        } else {
            rpcResponse = new DefaultRpcResponse(rpcRequest.getId(), 400,
                    "wait RpcResponse Timeout", null, null);
        }

        return rpcResponse;
    }

    @Override
    public InvokeFuture sendRequestWithCallback(RpcRequest rpcRequest,
                                                InvokeCallback callback) throws Exception {
        return sendRequestWithFuture(rpcRequest, DEFAULT_TIMEOUT, callback);
    }

    @Override
    public InvokeFuture sendRequestWithFuture(RpcRequest rpcRequest) throws Exception {
        return sendRequestWithFuture(rpcRequest, DEFAULT_TIMEOUT, null);
    }

    @Override
    public InvokeFuture sendRequestWithFuture(RpcRequest rpcRequest, long timeoutMillis) throws Exception {
        return sendRequestWithFuture(rpcRequest, timeoutMillis, null);
    }

    public InvokeFuture sendRequestWithFuture(RpcRequest rpcRequest,
                                              long timeoutMillis,
                                              InvokeCallback callback) throws Exception {
        DefaultInvokeFuture invokeFuture = new DefaultInvokeFuture(rpcRequest, callback);
        Map<Integer, InvokeFuture> invokeFutureMap = channel.attr(RpcResponseHandler.FUTURE).get();
        invokeFutureMap.put(invokeFuture.invokeId(), invokeFuture);

        int requestId = Integer.parseInt(rpcRequest.getId());

        if (timeoutMillis < DEFAULT_TIMEOUT) {
            timeoutMillis = DEFAULT_TIMEOUT;
        }
        Timeout timeout = timer.newTimeout(timeout1 -> {
            InvokeFuture future = invokeFutureMap.remove(requestId);
            if (future != null) {
                DefaultRpcResponse response = new DefaultRpcResponse(rpcRequest.getId());
                response.setCode(400);
                response.setMessage("wait RpcResponse timeout");
                future.setResponse(response);
                future.cancelTimeout();
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);
        invokeFuture.addTimeout(timeout);

        channel.writeAndFlush(rpcRequest).addListener(cf -> {
            if (!cf.isSuccess()) {
                InvokeFuture future = invokeFutureMap.remove(requestId);
                if (future != null) {
                    future.cancelTimeout();
                    future.setCause(cf.cause());
                    future.executeInvokeCallback();
                }
            }
        });

        return invokeFuture;
    }

    private void ensureOpen() {
        if (!this.isRunning && !this.connecting) {
            throw new IllegalStateException("send request failed, rpc client disconnected");
        }

        if (this.connecting) {
            waitStart();
        }
    }

    @Override
    public void addProcessor(NettyProcessor processor) {
        this.processors.add(processor);
    }

    @Override
    public void enableSsl(File jksFile, String password, boolean needClientAuth) {
        this.processors.add(new ClientSslProcessor(jksFile, password));
    }

    @Override
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void start() throws Exception {

        // 序列化
        this.serializer = this.serializer == null ? Serializer.JSON : this.serializer;
        this.processors.add(new SerializeProcessor(this.serializer, true));

        this.processors.forEach(processor -> processor.processBootstrap(bootstrap));

        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                processors.forEach(processor -> processor.processChannel(ch));
                ch.pipeline().addLast(new RpcResponseHandler());
            }
        });

        this.connecting = true;
        // 连接 rpc server
        this.channel = bootstrap.connect().addListener(future -> {
            if (future.isSuccess()) {
                this.isRunning = true;
                logger.info("connect rpc server success");
                if (logger.isDebugEnabled()) {
                    logger.debug("rpc client start success, remote server address {}",
                            this.channel.remoteAddress());
                }
            }
            this.connecting = false;
        }).sync().channel();

        try {
            // 等待关闭 channel
            this.channel.closeFuture().sync();
        } finally {
            this.isRunning = false;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (!this.isRunning) {
                return;
            }

            try {
                this.isRunning = false;
                // 关闭 channel
                this.channel.close().sync();
                // 释放连接池资源
                this.group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                logger.error("stop rpc client failed, ex: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isRunnable() {
        return this.isRunning;
    }

    @Override
    public boolean isConnecting() {
        return this.connecting;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.address;
    }

    @Override
    public Object invoke(String methodName, Object[] arguments, Type returnType, Metadata metadata) throws Throwable {
        String id = String.valueOf(idCounter.getAndIncrement());
        RpcRequest rpcRequest = new DefaultRpcRequest(id, methodName, arguments, metadata);

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
            logger.warn("Server returned result but returnType is null");
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
        if (rpcResponse.getCode() != 200) {
            throw new ClientInvocationException(rpcResponse.getCode(), rpcResponse.getMessage());
        }
    }

    @Override
    public void invoke(String methodName, Object argument) throws Throwable {
        invoke(methodName, new Object[]{argument}, null, new Metadata());
    }

    @Override
    public Object invoke(String methodName, Object[] arguments, Type returnType) throws Throwable {
        return invoke(methodName, arguments, returnType, new Metadata());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(String methodName, Object[] arguments, Class<T> clazz) throws Throwable {
        return (T) invoke(methodName, arguments, Type.class.cast(clazz));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(String methodName, Object[] arguments, Class<T> clazz, Metadata metadata)
            throws Throwable {
        return (T) invoke(methodName, arguments, Type.class.cast(clazz), metadata);
    }

    @Override
    public void run() {
        if (this.lastHealthCheckDate == null) {
            this.lastHealthCheckDate = new Date();
        }
        // 是否满足健康检查间隔
        long intervalSecond = DateUtil.between(this.lastHealthCheckDate, new Date(), DateUnit.SECOND);
        if (intervalSecond < DEFAULT_HEALTH_CHECK_INTERVAL) {
            return;
        }

        String id = String.valueOf(idCounter.getAndIncrement());
        RpcRequest rpcRequest = new DefaultRpcRequest(id, "health", null, new Metadata());
        try {
            sendRequest(rpcRequest);
            updateHealthCheckInfo();
        } catch (Exception exception) {
            logger.error("health check failed, ex: {}", exception.getMessage());
            if (++healthCheckFailureCount >= 3) {
                logger.info("close {} client, health check failure count >= 3", this.toString());
                this.close();
            }
        }
    }

    private void updateHealthCheckInfo() {
        this.lastHealthCheckDate = new Date();
        this.healthCheckFailureCount = 0;
    }

    private void waitStart() {
        try {
            long start = System.currentTimeMillis();
            while (!isRunning) {
                if (System.currentTimeMillis() - start > START_TIMEOUT) {
                    throw new IllegalStateException("client start timeout");
                }
                Thread.sleep(10L);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
