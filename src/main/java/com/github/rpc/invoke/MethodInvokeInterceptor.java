package com.github.rpc.invoke;

import java.util.function.Function;

/**
 * 方法调用拦截器, 通过代理实现
 *
 * @author Ray
 * @date created in 2022/3/3 19:41
 */
@FunctionalInterface
public interface MethodInvokeInterceptor extends
        Function<MethodInvokeDispatcher, MethodInvokeDispatcher> {
}

