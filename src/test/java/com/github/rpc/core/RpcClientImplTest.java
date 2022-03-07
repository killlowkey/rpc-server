package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.RpcServer;
import com.github.rpc.annotation.RpcComponent02;
import com.github.rpc.invoke.InvokeType;
import io.netty.channel.ChannelOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2022/3/6 10:40
 */
@RunWith(JUnit4.class)
public class RpcClientImplTest {

    RpcClient rpcClient;

    @Before
    public void startServer() throws Throwable {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .enableSaveAsmByteCode()
                .bind(8989)
                .registerComponent(RpcComponent02.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .build();

        new Thread(rpcServer::start).start();
        Thread.sleep(100L);
    }

    @Before
    public void startClient() throws Throwable {
        rpcClient = new RpcClientImpl(new InetSocketAddress("127.0.0.1", 8989));
        new Thread(() -> {
            try {
                rpcClient.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();

        Thread.sleep(2000L);
    }

    @Test
    public void requestTest() throws Throwable {
        String helloResult = rpcClient.invoke("RpcComponent02/hello", null, String.class);
        assertEquals("hello world", helloResult);

        rpcClient.invoke("RpcComponent02/helloMethod", null);
        String[]  getArrayValueResult = rpcClient.invoke("RpcComponent02/getArrayValue",
                new Object[]{new Integer[]{1, 2, 3}}, String[].class);
        assertArrayEquals(new String[]{"hello", "world"}, getArrayValueResult);
    }

}