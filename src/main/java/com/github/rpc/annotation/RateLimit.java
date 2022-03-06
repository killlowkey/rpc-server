package com.github.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 *
 * @author Ray
 * @date created in 2022/3/3 20:02
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RateLimit {
    TimeUnit value() default TimeUnit.SECONDS;

    int limit();
}
