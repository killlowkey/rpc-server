package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.invoke.InvokeType;
import io.netty.channel.ChannelOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

/**
 * @author Ray
 * @date created in 2022/3/6 11:08
 */
@RunWith(JUnit4.class)
public class RpcClientProxyTest {

    @Before
    public void startServer() throws Throwable {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .enableSaveAsmByteCode()
                .bind(8989)
                .registerComponent(PersonServiceImpl.class)
                .nettyChildOption(ChannelOption.SO_KEEPALIVE, true)
                .build();

        new Thread(rpcServer::start).start();
        Thread.sleep(1000L);
    }

    @Test
    public void invokeTest() throws InterruptedException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(address);
        PersonService personService = rpcClientProxy.createProxy(PersonService.class);

        assertEquals("hello world", personService.hello());
        assertEquals("hello tom", personService.say("tom"));
        assertEquals(10, personService.age());
    }
}