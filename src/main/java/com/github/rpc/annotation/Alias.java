package com.github.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ray
 * @date created in 2022/3/4 17:30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {
    String value();

    AliasStrategy strategy() default AliasStrategy.OVERWRITE;
}
