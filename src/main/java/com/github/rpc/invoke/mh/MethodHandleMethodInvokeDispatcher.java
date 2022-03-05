package com.github.rpc.invoke.mh;

import com.github.rpc.invoke.AbstractMethodInvokeDispatcher;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.invoke.MethodInvokeListener;
import org.tinylog.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/3 8:52
 */
public class MethodHandleMethodInvokeDispatcher extends AbstractMethodInvokeDispatcher {

    private final Map<String, MethodHandle> handleMap = new HashMap<>();

    public MethodHandleMethodInvokeDispatcher(Map<String, MethodContext> contextMap,
                                              List<MethodInvokeListener> interceptors) {
        super(contextMap, interceptors);
        this.initHandleMap();
    }

    private void initHandleMap() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        contextMap.forEach((name, context) -> {
            Method method = context.getMethod();
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            MethodType methodType = MethodType.methodType(returnType, parameterTypes);

            MethodHandle methodHandle = null;
            try {
                // 创建 MethodHandle
                methodHandle = lookup.findVirtual(method.getDeclaringClass(), methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                Logger.error("create {} methodHandle failed：{}", methodName, e.getMessage());
                e.printStackTrace();
            }

            this.handleMap.put(methodName, methodHandle);
        });
    }

    @Override
    public Object doInvoke(Object obj, String methodName, Object[] args) throws Throwable {
        MethodHandle methodHandle = this.handleMap.get(methodName);
        try {
            // params[0] = obj
            // params[i..] = args
            Object[] params = new Object[args.length + 1];
            params[0] = obj;
            // 从 args 拷贝元素到 params
            System.arraycopy(args, 0, params, 1, args.length);
            // 调用方法
            return methodHandle.invokeWithArguments(Arrays.asList(params));
        } catch (Throwable throwable) {
            Logger.error("execute {} method failed", methodName);
            throw throwable;
        }
    }
}
