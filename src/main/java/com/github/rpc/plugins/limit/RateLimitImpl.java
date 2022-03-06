package com.github.rpc.plugins.limit;

import com.github.rpc.annotation.RateLimitEntry;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2022/3/6 18:31
 */
public class RateLimitImpl implements RateLimit {

    private final Map<String, LimitEntry> limitEntryMap;
    private static final long DEFAULT_DURATION = 1;

    public RateLimitImpl(Map<String, RateLimitEntry> rateLimitEntryMap) {
        if (rateLimitEntryMap == null) {
            throw new IllegalArgumentException("rateLimitEntryMap cannot be null");
        }

        this.limitEntryMap = new HashMap<>();
        rateLimitEntryMap.forEach((name, entry) -> this.limitEntryMap.put(name, new LimitEntry(entry)));
    }

    @Override
    public boolean take(String name) {
        LimitEntry limitEntry = this.limitEntryMap.get(name);
        if (limitEntry == null) {
            return true;
        }

        try {
            limitEntry.getLock().lock();
            // 当前时间
            long current = System.currentTimeMillis();
            long interval = limitEntry.getTimeUnit().toMillis(DEFAULT_DURATION);
            long lastResetDate = limitEntry.getLastResetDate().getTime();
            if (current - lastResetDate >= interval) {
                limitEntry.setLastResetDate(new Date());
                limitEntry.getCounter().set(0);
                return true;
            }

            AtomicInteger counter = limitEntry.getCounter();
            return counter.incrementAndGet() <= limitEntry.getLimit();
        } finally {
            limitEntry.getLock().unlock();
        }
    }

    @Data
    static class LimitEntry {
        private final String name;
        private final int limit;
        private final TimeUnit timeUnit;
        private Date lastResetDate;
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger counter = new AtomicInteger();

        public LimitEntry(RateLimitEntry entry) {
            this.name = entry.getApi();
            this.limit = entry.getLimit();
            this.timeUnit = entry.getTimeUnit();
            this.lastResetDate = new Date();
        }

    }

}
