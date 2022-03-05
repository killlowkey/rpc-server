package com.github.rpc.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/5 15:29
 */
public class RpcUtil {

    static Map<String, Method> methodMap = new HashMap<>();

    public static void registerMethod(String name, Method method) {
        methodMap.put(name, method);
    }

    public static Method getMethod(String name) {
        return methodMap.get(name);
    }

}
