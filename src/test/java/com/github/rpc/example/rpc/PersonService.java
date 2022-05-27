package com.github.rpc.example.rpc;

import com.github.rpc.annotation.RpcClient;

/**
 * @author Ray
 * @date created in 2022/3/6 11:11
 */
@RpcClient
public interface PersonService {

    String hello();

    String say(String name);

    int age();
}
