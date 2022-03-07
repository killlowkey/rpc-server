package com.github.rpc.plugins.health;

import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeInterceptor;
import com.github.rpc.plugins.DispatcherProxy;

/**
 * 健康检查
 *
 * @author Ray
 * @date created in 2022/3/7 17:21
 */
public class HealthRequestInterceptor implements MethodInvokeInterceptor {

    @Override
    public MethodInvokeDispatcher apply(MethodInvokeDispatcher source) {
        return new HealthRequestProxy(source);
    }

    static class HealthRequestProxy extends DispatcherProxy {

        protected HealthRequestProxy(MethodInvokeDispatcher source) {
            super(source);
        }

        @Override
        public Object invoke(String methodName, Object... args) throws Throwable {
            if (methodName.equals("health")) {
                return "success";
            }

            return this.source.invoke(methodName, args);
        }

    }
}
