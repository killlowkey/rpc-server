package com.github.rpc.invoke.reflect;

import com.github.rpc.invoke.AbstractMethodInvokeDispatcher;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.invoke.MethodInvokeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/3 8:31
 */
public class ReflectMethodInvokeDispatcher extends AbstractMethodInvokeDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ReflectMethodInvokeDispatcher.class);

    public ReflectMethodInvokeDispatcher(Map<String, MethodContext> methodContextMap,
                                         List<MethodInvokeListener> interceptors) {
        super(methodContextMap, interceptors);
    }

    @Override
    public Object doInvoke(Object obj, String methodName, Object[] args) throws Throwable {
        MethodContext context = contextMap.get(methodName);
        try {
            return context.getMethod().invoke(obj, args);
        } catch (Exception ex) {
            logger.error("invoke {} method failed：{}", methodName, ex.getMessage());
            throw ex;
        }

    }
}
