package com.github.rpc.plugins.statistic;

import com.github.rpc.invoke.MethodContext;
import com.github.rpc.invoke.MethodInvokeListener;

import java.util.Date;

/**
 * 记录方法调用信息
 *
 * @author Ray
 * @date created in 2022/3/6 21:43
 */
public class MethodInvocationStatistics implements MethodInvokeListener {

    private final ThreadLocal<MethodInvocationInfo> threadLocal = new ThreadLocal<>();
    private final Storage storage;

    public MethodInvocationStatistics(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("storage cannot be null");
        }
        this.storage = storage;
    }

    @Override
    public void atBeforeInvoke(MethodContext context, Object... args) {
        MethodInvocationInfo info = new MethodInvocationInfo(context.getName(), args);
        this.threadLocal.set(info);
    }

    @Override
    public void atAfterInvoke(MethodContext context, Object result) {
        MethodInvocationInfo info = this.threadLocal.get();
        info.setResult(result);
        info.setEnd(new Date());
        try {
            this.storage.save(info);
        } finally {
            this.threadLocal.remove();
        }
    }

    @Override
    public void atInvokeException(MethodContext context, Throwable throwable) {
        MethodInvocationInfo info = this.threadLocal.get();
        info.setEx(throwable);
        info.setEnd(new Date());
        try {
            this.storage.save(info);
        } finally {
            this.threadLocal.remove();
        }
    }

}
