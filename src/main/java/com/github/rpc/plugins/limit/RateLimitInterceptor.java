package com.github.rpc.plugins.limit;

import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.exceptions.RpcServerException;
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

    public final RateLimit rateLimit;

    public RateLimitInterceptor(Map<String, RateLimitEntry> rateLimitEntryMap) {
        this.rateLimit = new RateLimitImpl(rateLimitEntryMap);
    }

    @Override
    public MethodInvokeDispatcher apply(MethodInvokeDispatcher methodInvokeDispatcher) {
        return new RateLimitProxy(methodInvokeDispatcher);
    }

    class RateLimitProxy extends DispatcherProxy {

        public RateLimitProxy(MethodInvokeDispatcher source) {
            super(source);
        }

        @Override
        public Object invoke(String methodName, Object... args) throws Throwable {
            if (!rateLimit.take(methodName)) {
                throw new RpcServerException(ErrorEnum.TRIGGER_RATE_LIMIT);
            }
            return this.source.invoke(methodName, args);
        }

    }
}
