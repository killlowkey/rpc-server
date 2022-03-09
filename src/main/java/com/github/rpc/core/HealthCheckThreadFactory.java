package com.github.rpc.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ray
 * @date created in 2022/3/9 21:27
 */
public class HealthCheckThreadFactory implements ThreadFactory {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
        String threadName = "health-check-" + ID_COUNTER.getAndIncrement();
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);
        return thread;
    }

}
