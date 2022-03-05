package com.github.rpc.core;

import com.github.rpc.RpcServer;
import com.github.rpc.annotation.RpcComponent02;
import com.github.rpc.invoke.InvokeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2022/3/5 19:36
 */
@RunWith(JUnit4.class)
public class RpcServerBuilderTest {

    @Test
    public void buildTest() {
        RpcServer rpcServer = new RpcServerBuilder()
                .invokeType(InvokeType.ASM)
                .bind(8989)
                .registerComponent(RpcComponent02.class)
//                .scanPackage("com.github.rpc")
                .build();

        rpcServer.start();
    }
}