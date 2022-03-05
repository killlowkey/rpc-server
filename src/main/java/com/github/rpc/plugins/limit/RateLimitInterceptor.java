package com.github.rpc.plugins.limit;

import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeInterceptor;
import com.github.rpc.plugins.DispatcherProxy;

import java.util.Map;

/**
 * 限流拦截器
 * 1、基于配置（yaml）
 * 2、基于注解（@LimitRate(String,int)）
 *
 * @author Ray
 * @date created in 2022/3/3 19:44
 */
public class RateLimitInterceptor implements MethodInvokeInterceptor {

    private final Map<String, RateLimitEntry> rateLimitEntryMap;

    public RateLimitInterceptor(Map<String, RateLimitEntry> rateLimitEntryMap) {
        this.rateLimitEntryMap = rateLimitEntryMap;
    }

    @Override
    public MethodInvokeDispatcher apply(MethodInvokeDispatcher methodInvokeDispatcher) {
        return new RateLimitProxy(methodInvokeDispatcher);
    }

    static class RateLimitProxy extends DispatcherProxy {

        public RateLimitProxy(MethodInvokeDispatcher source) {
            super(source);
        }

        @Override
        public Object invoke(String methodName, Object... args) throws Throwable {
            return this.source.invoke(methodName, args);
        }

    }
}
