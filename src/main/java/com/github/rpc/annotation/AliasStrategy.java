package com.github.rpc.annotation;

/**
 * 别名策略
 *
 * @author Ray
 * @date created in 2022/3/5 7:45
 */
public enum AliasStrategy {
    /**
     * Alias#value() 创建别名
     */
    OVERWRITE,

    /**
     * RpcService#value() + Alias#value() 创建别名
     */
    EXTEND;
}
