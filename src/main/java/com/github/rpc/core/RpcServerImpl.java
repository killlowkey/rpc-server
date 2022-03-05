package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tinylog.Logger;

import java.net.InetSocketAddress;

/**
 * @author Ray
 * @date created in 2022/3/5 12:51
 */
public class RpcServerImpl implements RpcServer {

    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup worker = new NioEventLoopGroup();
    private final MethodInvokeDispatcher dispatcher;
    private final InetSocketAddress address;

    public RpcServerImpl(MethodInvokeDispatcher dispatcher, InetSocketAddress address) {
        this.dispatcher = dispatcher;
        this.address = address;
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(this.boss, this.worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                // 请求与响应编解码器
                                .addLast(new RpcRequestCodecHandle())
                                .addLast(new RpcResponseCodecHandle())
                                // 处理请求
                                .addLast(new RpcRequestHandle(dispatcher));
                    }
                });

        try {
            // 等待绑定完成
            ChannelFuture channelFuture = bootstrap.bind(this.address).sync();
            Logger.info("start rpc server success");
            // 等待关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }
    }

    @Override
    public void stop() {
        try {
            this.boss.shutdownGracefully().sync();
            this.worker.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addListener() {

    }

}
