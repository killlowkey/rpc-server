package com.github.rpc.utils;

import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.*;

/**
 * @author Ray
 * @date created in 2022/3/4 17:26
 */
public class MethodUtil {

    /**
     * 过滤静态、抽象、私有、本地、protected 方法
     *
     * @param method 方法 实例
     * @return true 匹配，false 未匹配
     */
    public static boolean filterMethod(Method method) {
        int modifiers = method.getModifiers();
        return isStatic(modifiers) || isAbstract(modifiers) ||
                isPrivate(modifiers) || isNative(modifiers) ||
                isProtected(modifiers);
    }

}
