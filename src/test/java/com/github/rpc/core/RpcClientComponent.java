package com.github.rpc.core;

import com.github.rpc.annotation.RpcClient;

/**
 * @author Ray
 * @date created in 2022/3/6 11:11
 */
@RpcClient("service/")
public interface RpcClientComponent {

    String hello();

    String say(String name);

    int age();
}
