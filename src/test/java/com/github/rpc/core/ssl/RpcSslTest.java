package com.github.rpc.core.ssl;

import cn.hutool.core.io.resource.ClassPathResource;
import com.github.rpc.RpcServer;
import com.github.rpc.core.PersonService;
import com.github.rpc.core.RpcClientProxy;
import com.github.rpc.core.RpcServerBuilder;
import com.github.rpc.core.PersonServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

/**
 * @author Ray
 * @date created in 2022/3/7 9:27
 */
@RunWith(JUnit4.class)
public class RpcSslTest {


    @Before
    public void startServer() throws Exception {


        File file = new ClassPathResource("/certs/sChat.jks").getFile();

        RpcServer rpcServer = new RpcServerBuilder()
                .bind(8989)
                .registerComponent(PersonServiceImpl.class)
                .enableSSL(file, "sNetty", true)
                .build();

        new Thread(rpcServer::start).start();
        Thread.sleep(1000L);
    }

    @Test
    public void clientTest() throws Exception {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);

        File file = new ClassPathResource("/certs/cChat.jks").getFile();
        PersonService clientComponent = new RpcClientProxy(address)
                .enableSsl(file, "sNetty")
                .createProxy(PersonService.class);

        assertEquals("hello world", clientComponent.hello());
        assertEquals("hello tom", clientComponent.say("tom"));
        assertEquals(10, clientComponent.age());
    }

}
