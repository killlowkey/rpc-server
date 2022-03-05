package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.utils.MethodUtil;
import org.tinylog.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 别名注解处理器
 * 1、EXTEND 模式
 * 2、OVERWRITE 模式
 *
 * 处理后要 clone 一份 MethodContext 更新 name，之后添加到组件中
 *
 * @author Ray
 * @date created in 2022/3/4 17:31
 */
public class AliasAnnotationProcessor implements AnnotationProcessor {

    @Override
    public void process(ScanContext context, Annotation annotation) {
        Alias alias = (Alias) annotation;

        if (alias.value().isBlank()) {
            if (Logger.isDebugEnabled()) {
                String name = context.getMethod().getName();
                Logger.debug("{} method alias value is null, alias processor exit", name);
            }
            return;
        }

        if (MethodUtil.filterMethod(context.getMethod())) {
            return;
        }

        // 获取该方法组件名称
        String componentName = getComponentName(context.getClazz(), context.getMethod());
        if (componentName == null) {
            return;
        }

        // 获取方法的 MethodContext
        Map<String, MethodContext> rpcComponents = context.getConfiguration().getRpcComponents();
        MethodContext methodContext = rpcComponents.get(componentName);
        if (methodContext == null) {
            return;
        }

        RpcServiceConfiguration configuration = context.getConfiguration();
        if (alias.strategy() == AliasStrategy.OVERWRITE) {
            // OVERWRITE 模式：别名组件名称为 Alias#value()
            MethodContext newMethod = methodContext.clone();
            newMethod.setName(alias.value());
            rpcComponents.put(alias.value(), newMethod);

            updateRateLimit(configuration, componentName, alias.value());
        } else if (alias.strategy() == AliasStrategy.EXTEND) {
            // EXTEND 模式：别名组件名称为 RpcService#value() + Alias#value()
            String name = getOverwriteComponentName(methodContext, alias);
            if (name == null) {
                return;
            }

            MethodContext newMethod = methodContext.clone();
            newMethod.setName(name);
            rpcComponents.put(name, newMethod);

            updateRateLimit(configuration, componentName, name);
        }

    }

    private String getOverwriteComponentName(MethodContext context, Alias alias) {
        Class<?> clazz = context.getMethod().getDeclaringClass();
        String rpcServiceName = getRpcServiceName(clazz);
        if (rpcServiceName == null) {
            return null;
        }

        return rpcServiceName + alias.value();
    }

    private String getComponentName(Class<?> clazz, Method method) {
        String rpcServiceName = getRpcServiceName(clazz);
        if (rpcServiceName == null) {
            return null;
        }
        return rpcServiceName + method.getName();
    }

    private String getRpcServiceName(Class<?> clazz) {
        RpcService rpcService = clazz.getAnnotation(RpcService.class);
        if (rpcService == null) {
            return null;
        }
        return rpcService.value();
    }

    private void updateRateLimit(RpcServiceConfiguration configuration,
                                 String oldComponentName,
                                 String newComponentName) {
        Map<String, RateLimitEntry> rateLimitEntryMap = configuration.getRateLimitEntryMap();
        RateLimitEntry rateLimitEntry = rateLimitEntryMap.get(oldComponentName);
        if (rateLimitEntry == null) {
            return;
        }

        rateLimitEntryMap.put(newComponentName, rateLimitEntry);
    }

    @Override
    public boolean isMatcher(Annotation annotation) {
        return Alias.class == annotation.annotationType();
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
