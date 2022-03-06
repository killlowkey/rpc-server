package com.github.rpc.annotation;

import java.util.concurrent.TimeUnit;

/**
 * @author Ray
 * @date created in 2022/3/4 17:38
 */
@RpcService("RpcComponent02/")
@RateLimit(limit = 5, value = TimeUnit.MINUTES)
public class RpcComponent02 {

    @Alias(value = "helloAlias", strategy = AliasStrategy.OVERWRITE)
    public String hello() {
        return "hello world";
    }

    @Alias(value = "helloMethodAlias", strategy = AliasStrategy.EXTEND)
    public void helloMethod() {

    }

    public String[] getArrayValue(int[] args) {
        return new String[]{"hello", "world"};
    }

}
