package com.github.rpc.annotation;

import java.util.concurrent.TimeUnit;

/**
 * @author Ray
 * @date created in 2022/3/4 14:37
 */
@RpcService("rpc/component/")
@RateLimit(limit = 100)
public class RpcComponent {

    @RateLimit(value = TimeUnit.HOURS, limit = 50)
    @Alias("alias say rpc")
    public void say() {

    }

    @Alias("hello")
    public static void staticMethod() {

    }

    private void privateMethod() {

    }

    protected void protectedMethod() {
    }

}
