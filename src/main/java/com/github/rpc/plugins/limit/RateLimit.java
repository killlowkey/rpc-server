package com.github.rpc.plugins.limit;

/**
 * @author Ray
 * @date created in 2022/3/6 18:29
 */
public interface RateLimit {

    boolean take(String name);

}
