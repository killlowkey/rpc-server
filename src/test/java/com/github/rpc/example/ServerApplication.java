package com.github.rpc.example;

import com.github.rpc.RpcServer;
import com.github.rpc.annotation.RpcComponent02;
import com.github.rpc.core.PersonServiceImpl;
import com.github.rpc.core.RpcServerBuilder;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.serializer.Serializer;
import io.netty.channel.ChannelOption;

/**
 * @author Ray
 * @date created in 2022/5/24 11:15
 */
public class ServerApplication {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .bind(8989)
                .registerComponent(PersonServiceImpl.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .serializer(Serializer.PROTOBUF)
                .build();
        rpcServer.start();
    }
}
