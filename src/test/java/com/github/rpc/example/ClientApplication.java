package com.github.rpc.example;

import com.github.rpc.core.RpcClientProxy;
import com.github.rpc.example.rpc.OrderService;
import com.github.rpc.example.rpc.PersonService;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import com.github.rpc.serializer.Serializer;

import java.net.InetSocketAddress;
import java.util.Arrays;


/**
 * @author Ray
 * @date created in 2022/5/24 11:19
 */
public class ClientApplication {
    public static void main(String[] args) throws InterruptedException {
//        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);

//        String zookeeper = args[0];

        RpcClientProxy clientProxy = new RpcClientProxy()
//                .zookeeper(zookeeper)
                .rpcServerAddress(Arrays.asList(new InetSocketAddress("127.0.0.1", 9090)))
                .loadBalance(LoadBalanceStrategy.ROTATION)
                .serialize(Serializer.PROTOBUF);

        PersonService service = clientProxy.createProxy(PersonService.class);
        System.out.println(service.hello());
        System.out.println(service.say("ray"));

        OrderService orderService = clientProxy.createProxy(OrderService.class);
        System.out.println(orderService.getAllOrders());
    }
}
