package com.github.rpc.core;

import com.github.rpc.RpcClient;
import com.github.rpc.RpcServer;
import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ray
 * @date created in 2022/3/5 13:37
 */
@RunWith(JUnit4.class)
public class RpcServerImplTest {

    String packetName = "com.github.rpc.core";

    @Before
//    @Test
    public void startTest() throws Exception {
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner(packetName, configuration);
        scanner.scan();

        MethodInvokeDispatcher dispatcher = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.ASM)
                .build();

        RpcServer rpcServer = new RpcServerImpl(dispatcher, new InetSocketAddress(8989));
//        rpcServer.start();
        new Thread(rpcServer::start).start();
        Thread.sleep(1000L);
    }

    @Test
    public void test() throws Throwable {
        RpcClient rpcClient = new RpcClientImpl(new InetSocketAddress("127.0.0.1", 8989));
        new Thread(() -> {
            try {
                rpcClient.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();

        Thread.sleep(1000L);

//        RpcRequest rpcRequest = new RpcRequest("1", "2.0", "hello", null);
//        System.out.println(rpcClient.sendRequest(rpcRequest));

        String result = rpcClient.invoke("RpcExample/hello", null, String.class);
        assertEquals("hello rpc", result);
    }
}