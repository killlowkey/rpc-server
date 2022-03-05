package com.github.rpc.core;

import com.github.rpc.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;
import org.tinylog.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2022/3/5 10:33
 */
public class RpcClientImpl implements RpcClient {

    private final String host;
    private final int port;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final ReentrantLock lock = new ReentrantLock();
    // 存放请求
    private final ArrayBlockingQueue<RpcRequest> sendingQueue = new ArrayBlockingQueue<>(16);
    // 存放响应
    private final ArrayBlockingQueue<RpcResponse> responseReceivers = new ArrayBlockingQueue<>(16);
    private Channel channel;

    public RpcClientImpl(String host, int port) {
        if (StringUtil.isNullOrEmpty(host)) {
            Logger.error("host is empty, create RpcClientImpl failed");
            throw new IllegalArgumentException("host is empty, create RpcClientImpl failed");
        }

        if (port < 0) {
            Logger.error("port is less than 0 , create RpcClientImpl failed");
            throw new IllegalArgumentException("port is less than 0, create RpcClientImpl failed");
        }

        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        try {
            lock.lock();
            if (this.responseReceivers.isEmpty()) {
                this.channel.writeAndFlush(rpcRequest);
            } else {
                this.sendingQueue.offer(rpcRequest);
            }
        } finally {
            lock.unlock();
        }

        RpcResponse rpcResponse = this.responseReceivers.take();
        sendNextRequest();
        return rpcResponse;
    }

    private void sendNextRequest() {
        if (!sendingQueue.isEmpty()) {
            this.channel.writeAndFlush(this.sendingQueue.poll());
        }
    }

    @Override
    public void start() throws Exception {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .remoteAddress(new InetSocketAddress(this.host, this.port))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                // 请求和响应编解码器
                                // note：先响应编码器，后请求编码器
                                // 响应编码器的 decode 数据之后，请求编码器的 decode 就不会进行工作
                                // 所以位置一定要正确，否则无法正确编码
                                .addLast(new RpcResponseCodecHandle())
                                .addLast(new RpcRequestCodecHandle())
                                // 响应处理器
                                .addLast(new RpcResponseHandle(responseReceivers));
                    }
                });

        // 连接 channel
        ChannelFuture channelFuture = bootstrap.connect().sync();
        this.channel = channelFuture.channel();
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

}
