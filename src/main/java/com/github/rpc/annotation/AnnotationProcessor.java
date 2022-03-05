package com.github.rpc.annotation;

import java.lang.annotation.Annotation;

/**
 * @author Ray
 * @date created in 2022/3/4 12:50
 */
public interface AnnotationProcessor {

    /**
     * 处理注解
     *
     * @param context    注解上下文信息
     * @param annotation 注解
     */
    void process(ScanContext context, Annotation annotation);

    /**
     * 匹配注解是否由该处理器处理
     *
     * @param annotation 注解
     * @return true 匹配，false 不匹配
     */
    boolean isMatcher(Annotation annotation);

    /**
     * 获取处理器优先级，小的值优先级更高
     *
     * @return 优先级
     */
    int getPriority();

}
