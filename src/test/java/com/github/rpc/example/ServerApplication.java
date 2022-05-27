package com.github.rpc.example;

import com.github.rpc.RpcServer;
import com.github.rpc.core.RpcServerBuilder;
import com.github.rpc.example.rpc.OrderServiceImpl;
import com.github.rpc.example.rpc.PersonServiceImpl;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.serializer.Serializer;
import io.netty.channel.ChannelOption;

/**
 * @author Ray
 * @date created in 2022/5/24 11:15
 */
public class ServerApplication {
    public static void main(String[] args) {
        String zookeeper = args[0];
        int port = Integer.parseInt(args[1]);

        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.METHOD_HANDLE)
                .bind(port)
                .registerComponent(PersonServiceImpl.class, OrderServiceImpl.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .zookeeper(zookeeper)
                .serialize(Serializer.PROTOBUF)
                .build();
        rpcServer.start();
    }
}
