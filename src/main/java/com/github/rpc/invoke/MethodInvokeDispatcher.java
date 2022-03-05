package com.github.rpc.invoke;

/**
 * @author Ray
 * @date created in 2022/3/3 7:53
 */
public interface MethodInvokeDispatcher {

    /**
     * 调用方法
     *
     * @param methodName 方法名
     * @param args       方法参数
     * @return 方法返回值
     */
    Object invoke(String methodName, Object... args) throws Throwable;

    void addInvokeListener(MethodInvokeListener listener);

    boolean removeInvokeListener(MethodInvokeListener listener);

}
