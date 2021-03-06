package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.annotation.RpcComponent02;
import com.github.rpc.invoke.InvokeType;
import io.netty.channel.ChannelOption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ray
 * @date created in 2022/3/5 19:36
 */
@RunWith(JUnit4.class)
public class RpcServerBuilderTest {

    @Test
    public void startServerTest() {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .enableSaveAsmByteCode()
                .bind(8989)
                .registerComponent(RpcComponent02.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .enableInvocationStatistics(System.out::println)
//                .scanPackage("com.github.rpc")
                .build();

        rpcServer.start();
    }

}