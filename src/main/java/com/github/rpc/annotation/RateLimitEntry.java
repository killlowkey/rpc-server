package com.github.rpc.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author Ray
 * @date created in 2022/3/3 20:08
 */
@Data
@AllArgsConstructor
public class RateLimitEntry {
    private final String api;
    private final int limit;
    private final TimeUnit timeUnit;
}
