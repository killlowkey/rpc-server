package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.loadbalance.LoadBalanceStrategy;
import io.netty.channel.ChannelOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2022/3/6 15:43
 */
@RunWith(JUnit4.class)
public class RpcLoadBalanceClientImplTest {
    @Before
    public void startServer1() throws Throwable {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .enableSaveAsmByteCode()
                .bind(8888)
                .registerComponent(PersonServiceImpl.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .build();

        new Thread(rpcServer::start).start();
        Thread.sleep(1000L);
    }

    @Before
    public void startServer2() throws Throwable {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .enableSaveAsmByteCode()
                .bind(9999)
                .registerComponent(PersonServiceImpl.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .build();

        new Thread(rpcServer::start).start();
        Thread.sleep(1000L);
    }

    @Test
    public void loadBalanceTest() {
        InetSocketAddress address1 = new InetSocketAddress("127.0.0.1", 8888);
        InetSocketAddress address2 = new InetSocketAddress("127.0.0.1", 9999);
        PersonService clientComponent = new RpcClientProxy()
                .rpcServerAddress(Arrays.asList(address1, address2))
                .loadBalance(LoadBalanceStrategy.ROTATION)
                .createProxy(PersonService.class);

        assertEquals("hello world", clientComponent.hello());
        assertEquals("hello tom", clientComponent.say("tom"));
        assertEquals(10, clientComponent.age());
    }
}