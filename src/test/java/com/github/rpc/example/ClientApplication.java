package com.github.rpc.example;

import com.github.rpc.core.PersonService;
import com.github.rpc.core.RpcClientProxy;
import com.github.rpc.serializer.Serializer;

import java.net.InetSocketAddress;

/**
 * @author Ray
 * @date created in 2022/5/24 11:19
 */
public class ClientApplication {
    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);
        PersonService service = new RpcClientProxy(address)
                .serializer(Serializer.PROTOBUF)
                .createProxy(PersonService.class);

        System.out.println(service.hello());
        System.out.println(service.say("ray"));
    }
}
