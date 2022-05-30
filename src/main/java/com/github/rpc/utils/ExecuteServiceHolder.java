package com.github.rpc.utils;

import io.netty.util.NettyRuntime;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ray
 * @date created in 2022/5/30 18:31
 */
public class ExecuteServiceHolder {
    private static ExecutorService INSTANCE;

    public static void init() {
        if (INSTANCE != null) {
            return;
        }

        synchronized (ExecuteServiceHolder.class) {
            if (INSTANCE == null) {
                // 处理器个数x2
                int threadSize = NettyRuntime.availableProcessors() * 2;
                INSTANCE = new ScheduledThreadPoolExecutor(threadSize, new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "Rpc-Server-IoThread-" + counter.getAndIncrement());
                        thread.setDaemon(true);
                        return thread;
                    }
                });
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::shutdown));
    }

    public static ExecutorService getInstance() {
        if (INSTANCE == null) {
            init();
        }

        return INSTANCE;
    }

}
