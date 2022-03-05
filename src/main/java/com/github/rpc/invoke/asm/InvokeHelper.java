package com.github.rpc.invoke.asm;

import com.github.rpc.invoke.MethodContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Ray
 * @date created in 2022/3/3 16:33
 */
public class InvokeHelper {

    private static final Map<String, MethodContext> METHOD_CONTEXT_MAP = new HashMap<>();
    private static final Map<String, Integer> INDEX = new HashMap<>();

    public static void initHelper(Map<String, MethodContext> map) {
        METHOD_CONTEXT_MAP.putAll(map);
        initIndex();
    }

    private static void initIndex() {
        AtomicInteger counter = new AtomicInteger();
        METHOD_CONTEXT_MAP.forEach((name, context) -> INDEX.put(name, counter.getAndIncrement()));
    }

    public static Method getMethod(int index) {
        AtomicReference<String> reference = new AtomicReference<>("");
        INDEX.forEach((name, i) -> {
            if (index == i) {
                reference.set(name);
            }
        });

        MethodContext methodContext = METHOD_CONTEXT_MAP.get(reference.get());
        if (methodContext == null) {
            return null;
        }

        return methodContext.getMethod();
    }

    public static int getMethodIndex(String methodName) {
        Integer integer = INDEX.get(methodName);
        if (integer != null) {
            return integer;
        }

        return -1;
    }

    public static String formatMsg(String methodName) {
        return String.format("not found %s method", methodName);
    }
}
