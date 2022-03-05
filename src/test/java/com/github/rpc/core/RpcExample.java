package com.github.rpc.core;

import com.github.rpc.annotation.RpcService;

/**
 * @author Ray
 * @date created in 2022/3/5 13:38
 */
@RpcService("RpcExample/")
public class RpcExample {

    public String hello() {
        return "hello rpc";
    }

    public int[] say(int[] ages) {
        return ages;
    }

}
