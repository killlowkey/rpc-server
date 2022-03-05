package com.github.rpc.invoke;

/**
 * 方法调用拦截器（可有可无）
 *
 * @author Ray
 * @date created in 2022/3/3 8:42
 */
public interface MethodInvokeListener {

    /**
     * 方法调用前
     *
     * @param context method context
     * @param args    方法参数
     */
    void atBeforeInvoke(MethodContext context, Object... args);

    /**
     * 方法调用后
     *
     * @param context method context
     */
    void atAfterInvoke(MethodContext context);

    /**
     * 出现异常
     *
     * @param context   method context
     * @param throwable 异常信息
     */
    void atInvokeException(MethodContext context, Throwable throwable);

}
