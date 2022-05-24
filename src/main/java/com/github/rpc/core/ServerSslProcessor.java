package com.github.rpc.core;

import com.github.rpc.utils.SslContextUtil;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.util.Objects;

/**
 * @author Ray
 * @date created in 2022/3/7 8:34
 */
public class ServerSslProcessor implements NettyProcessor {

    private final File jksFile;
    private final boolean needClientAuth;
    private final String keyStorePass;

    public ServerSslProcessor(File jksFile, String keyStorePass, boolean needClientAuth) {
        if (jksFile == null) {
            throw new IllegalArgumentException("jks file cannot be null");
        }

        this.jksFile = jksFile;
        this.needClientAuth = needClientAuth;
        this.keyStorePass = keyStorePass;
    }

    @Override
    public void processChannel(Channel channel) {
        SSLContext sslContext = SslContextUtil.getSslContext(this.jksFile, this.keyStorePass);
        SSLEngine engine = Objects.requireNonNull(sslContext).createSSLEngine();
        engine.setUseClientMode(false); //3
        // false 单向认证，true 双向认证
        engine.setNeedClientAuth(this.needClientAuth);
        channel.pipeline().addFirst("ssl", new SslHandler(engine));  //4
    }

}
