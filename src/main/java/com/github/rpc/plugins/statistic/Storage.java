package com.github.rpc.plugins.statistic;

/**
 * 存储接口
 * 子类实现时，不要在当前线程进行持久化操作，
 * 这样会阻塞 netty 线程，从而影响性能
 *
 * @author Ray
 * @date created in 2022/3/6 21:55
 */
public interface Storage {
    void save(MethodInvocationInfo info);
}
