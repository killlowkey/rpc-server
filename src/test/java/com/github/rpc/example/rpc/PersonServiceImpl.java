package com.github.rpc.example.rpc;

import com.github.rpc.annotation.RpcService;

/**
 * @author Ray
 * @date created in 2022/3/6 11:10
 */
@RpcService
public class PersonServiceImpl implements PersonService {

    public String hello() {
        return "hello world";
    }

    public String say(String name) {
        return "hello " + name;
    }

    public int age() {
        return 10;
    }

}
