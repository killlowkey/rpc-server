package com.github.rpc.core;

import com.github.rpc.annotation.RpcService;

/**
 * @author Ray
 * @date created in 2022/3/6 11:10
 */
@RpcService("service/")
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
