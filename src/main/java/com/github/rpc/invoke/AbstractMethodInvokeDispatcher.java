package com.github.rpc.invoke;

import com.github.rpc.exceptions.MethodNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/3 8:01
 */
public abstract class AbstractMethodInvokeDispatcher implements MethodInvokeDispatcher {

    protected final Map<String, MethodContext> contextMap;
    protected final List<MethodInvokeListener> listeners;

    protected AbstractMethodInvokeDispatcher(Map<String, MethodContext> contextMap,
                                             List<MethodInvokeListener> listeners) {
        this.contextMap = contextMap;
        this.listeners = listeners;
    }

    @Override
    public Object invoke(String methodName, Object... args) throws Throwable {
        MethodContext context = contextMap.get(methodName);
        if (context == null) {
            throw new MethodNotFoundException(String.format("not found %s method", methodName));
        }

        // 方法调用前
        listeners.forEach(interceptor -> interceptor.atBeforeInvoke(context, args));
        Object result;
        try {
            result = doInvoke(context.getObj(), methodName, args);
        } catch (Throwable throwable) {
            // 发生异常
            listeners.forEach(interceptor -> interceptor.atInvokeException(context, throwable));
            throw throwable;
        }

        // 方法调用后
        listeners.forEach(interceptor -> interceptor.atAfterInvoke(context, result));
        return result;
    }


    public abstract Object doInvoke(Object obj, String methodName, Object[] args) throws Throwable;

    @Override
    public void addInvokeListener(MethodInvokeListener listener) {
        if (!this.listeners.contains(listener)) {
             this.listeners.add(listener);
        }
    }

    @Override
    public boolean removeInvokeListener(MethodInvokeListener listener) {
        if (!this.listeners.contains(listener)) {
            return false;
        }
        this.listeners.remove(listener);
        return true;
    }
}
