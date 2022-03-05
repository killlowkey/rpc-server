package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.utils.MethodUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * RateLimit 注解处理器
 * RateLimit 可以标注在类和方法上，所以需要分两步处理
 * 1、对类中所有的组件，统一设置限流信息
 * 2、仅更新方法组件限流信息
 *
 * @author Ray
 * @date created in 2022/3/4 16:49
 */
public class RateLimitAnnotationProcessor implements AnnotationProcessor {

    @Override
    public void process(ScanContext context, Annotation annotation) {
        RateLimit rateLimit = (RateLimit) annotation;

        // 判断注解标注在类上还是方法
        if (context.getClazz() != null) {
            processClass(context, rateLimit);
        } else {
            processMethod(context, rateLimit);
        }
    }

    private void processClass(ScanContext context, RateLimit rateLimit) {
        RpcService rpcService = context.getClazz().getAnnotation(RpcService.class);
        Class<?> clazz = context.getClazz();
        RpcServiceConfiguration configuration = context.getConfiguration();
        Map<String, MethodContext> rpcComponents = configuration.getRpcComponents();
        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();

        rpcComponents.forEach((name, methodContext) -> {
            if (name.startsWith(rpcService.value())) {
                Class<?> declaringClass = methodContext.getMethod().getDeclaringClass();
                if (declaringClass == clazz) {
                    RateLimitEntry rateLimitEntry = new RateLimitEntry(name, rateLimit.limit(), rateLimit.value());
                    rateLimitEntryMap.put(name, rateLimitEntry);
                }
            }
        });
    }

    private void processMethod(ScanContext context, RateLimit rateLimit) {
        // 方法所属的类没有 RpcService 注解，则不需要后续处理
        RpcService rpcService = context.getClazz().getAnnotation(RpcService.class);
        if (rpcService == null) {
            return;
        }

        Method method = context.getMethod();
        RpcServiceConfiguration configuration = context.getConfiguration();
        if (MethodUtil.filterMethod(method)) {
            return;
        }

        // 找到该方法对应的 MethodContext
        Map<String, MethodContext> rpcComponents = configuration.getRpcComponents();
        MethodContext methodContext = rpcComponents.get(rpcService.value() + method.getName());
        if (methodContext == null) {
            return;
        }

        // 设置限流信息
        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();
        String name = methodContext.getName();
        RateLimitEntry rateLimitEntry = new RateLimitEntry(name, rateLimit.limit(), rateLimit.value());
        rateLimitEntryMap.put(name, rateLimitEntry);
    }


    @Override
    public boolean isMatcher(Annotation annotation) {
        return RateLimit.class == annotation.annotationType();
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
