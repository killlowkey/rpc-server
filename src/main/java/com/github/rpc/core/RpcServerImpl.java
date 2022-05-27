package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.core.handle.RpcRequestHandler;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.registry.Registry;
import com.github.rpc.serializer.Serializer;
import com.github.rpc.serializer.SerializeProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(RpcServerImpl.class);

    private final EventLoopGroup boss = new NioEventLoopGroup();
    private final EventLoopGroup worker = new NioEventLoopGroup();
    private final List<NettyProcessor> processors = new ArrayList<>();
    private final Set<RpcServerListener> listenerSet = new HashSet<>();

    private Serializer serializer;
    private ServerBootstrap bootstrap;
    private MethodInvokeDispatcher dispatcher;
    private InetSocketAddress address;
    private Registry registry;
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

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public void setDispatcher(MethodInvokeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void addProcessor(NettyProcessor processor) {
        this.processors.add(processor);
    }

    @Override
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {

        if (this.dispatcher == null) {
            throw new IllegalStateException("dispatcher cannot be null");
        }

        if (this.address == null) {
            throw new IllegalStateException("address cannot be null");
        }

        // 序列化
        this.serializer = this.serializer == null ? Serializer.JSON : this.serializer;
        this.processors.add(new SerializeProcessor(serializer, false));

        this.processors.forEach(processor -> processor.processServerBootstrap(bootstrap));

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        processors.forEach(processor -> processor.processChannel(ch));
                        ch.pipeline().addLast(new RpcRequestHandler(dispatcher, registry));
                    }
                });

        try {
            // 等待绑定完成
            Channel channel = bootstrap.bind(this.address).addListener(future -> {
                if (future.isSuccess()) {
                    this.runnable = true;
                    logger.info("start rpc server success");
                    // 调用启动监听器
                    this.listenerSet.forEach(RpcServerListener::onStartCompleted);
                } else {
                    logger.error("start rep server failed, error：{}", future.cause().getMessage());
                }
            }).sync().channel();
            // 等待关闭
            channel.closeFuture().sync();
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
                    logger.info("stop rpc server success");
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

    public void serialize(Serializer serializer) {
        this.serializer = serializer;
    }

}
