package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.core.handle.RpcRequestCodec;
import com.github.rpc.core.handle.RpcRequestHandler;
import com.github.rpc.core.handle.RpcResponseCodec;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.tinylog.Logger;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ray
 * @date created in 2022/3/5 12:51
 */
public class RpcServerImpl implements RpcServer {

    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup worker = new NioEventLoopGroup();
    private final List<NettyServerProcessor> processors = new ArrayList<>();
    private final Set<RpcServerListener> listenerSet = new HashSet<>();

    private ServerBootstrap bootstrap;
    private MethodInvokeDispatcher dispatcher;
    private InetSocketAddress address;
    private boolean runnable;

    public RpcServerImpl(MethodInvokeDispatcher dispatcher, InetSocketAddress address) {
        this.dispatcher = dispatcher;
        this.address = address;
        this.initBootStrap();
    }

    public RpcServerImpl() {
        this.initBootStrap();
    }

    private void initBootStrap() {
        bootstrap = new ServerBootstrap()
                .group(this.boss, this.worker)
                .channel(NioServerSocketChannel.class);
    }

    public <T> void childOption(ChannelOption<T> option, T value) {
        this.bootstrap.childOption(option, value);
    }

    public <T> void serverOption(ChannelOption<T> option, T value) {
        this.bootstrap.option(option, value);
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public void setDispatcher(MethodInvokeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void addProcessor(NettyServerProcessor processor) {
        this.processors.add(processor);
    }

    @Override
    public void start() {

        if (this.dispatcher == null) {
            throw new IllegalStateException("dispatcher cannot be null");
        }

        if (this.address == null) {
            throw new IllegalStateException("address cannot be null");
        }

        this.processors.forEach(processor -> processor.process(bootstrap));

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        processors.forEach(processor -> processor.processChannel(ch));
                        ch.pipeline()
                                .addLast(new JsonObjectDecoder())
                                // 请求与响应编解码器
                                .addLast(new RpcRequestCodec())
                                .addLast(new RpcResponseCodec())
                                // 处理请求
                                .addLast(new RpcRequestHandler(dispatcher));
                    }
                });

        try {
            // 等待绑定完成
            ChannelFuture channelFuture = bootstrap.bind(this.address).sync();
            this.runnable = true;
            // 调用启动监听器
            this.listenerSet.forEach(RpcServerListener::onStartCompleted);
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
        synchronized (this) {
            if (this.runnable) {
                try {
                    this.runnable = false;
                    // 调用停止监听器
                    this.listenerSet.forEach(RpcServerListener::onStopCompleted);
                    this.boss.shutdownGracefully().sync();
                    this.worker.shutdownGracefully().sync();
                    Logger.info("stop rpc server success");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void addListener(RpcServerListener listener) {
        this.listenerSet.add(listener);
    }

    @Override
    public boolean isRunnable() {
        return this.runnable;
    }

    @Override
    public void enableSsl(File jksFile, String password, boolean needClientAuth) {
        this.addProcessor(new ServerSslProcessor(jksFile, password, needClientAuth));
    }

}
