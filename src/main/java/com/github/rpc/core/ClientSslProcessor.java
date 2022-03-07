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
 * @date created in 2022/3/7 8:56
 */
public class ClientSslProcessor implements NettyClientProcessor {

    private final File jksFile;
    private final String keyStorePass;

    public ClientSslProcessor(File jksFile, String keyStorePass) {
        if (jksFile == null) {
            throw new IllegalArgumentException("jks file cannot be null");
        }

        this.jksFile = jksFile;
        this.keyStorePass = keyStorePass;
    }

    @Override
    public void processChannel(Channel channel) {
        SSLContext sslContext = SslContextUtil.getSslContext(this.jksFile, this.keyStorePass);
        SSLEngine engine = Objects.requireNonNull(sslContext).createSSLEngine();
        engine.setUseClientMode(true); //3
        channel.pipeline().addFirst("ssl", new SslHandler(engine));  //4
    }

}
