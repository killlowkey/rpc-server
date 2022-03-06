package com.github.rpc;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/6 9:52
 */
public interface InvokeOperation {

    void invoke(String method, Object argument) throws Throwable;

    Object invoke(String methodName, Object[] arguments, Type returnType) throws Throwable;

    Object invoke(String methodName, Object[] arguments, Type returnType, Map<String, String> extraHeaders) throws Throwable;

    <T> T invoke(String methodName, Object[] arguments, Class<T> clazz) throws Throwable;

    <T> T invoke(String methodName, Object[] arguments, Class<T> clazz, Map<String, String> extraHeaders) throws Throwable;

}
