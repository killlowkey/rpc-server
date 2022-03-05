package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.utils.MethodUtil;
import org.tinylog.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/4 12:51
 */
public class RpcServiceAnnotationProcessor implements AnnotationProcessor {

    private RpcServiceConfiguration configuration;

    @Override
    public void process(ScanContext context, Annotation annotation) {
        RpcService rpcService = (RpcService) annotation;
        this.configuration = context.getConfiguration();
        if (this.configuration == null) {
            Logger.error("configuration is null, RpcServiceAnnotationProcessor execute failed");
            throw new RuntimeException("configuration is null");
        }

        Class<?> clazz = context.getClazz();
        Object obj = newInstance(clazz);

        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            if (MethodUtil.filterMethod(method)) {
                return;
            }
            registerRpc(rpcService.value(), obj, method);
        });
    }

    private Object newInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            Logger.error("not found {} default constructor", clazz.getSimpleName());
            e.printStackTrace();
        } catch (Exception e) {
            Logger.error("instance {} object failed  ", clazz.getSimpleName());
            e.printStackTrace();
        }

        return null;
    }

    private void registerRpc(String parentName, Object obj, Method method) {
        Map<String, MethodContext> rpcComponents = configuration.getRpcComponents();
        String name = parentName + method.getName();
        if (rpcComponents.containsKey(name)) {
            return;
        }

        MethodContext context = new MethodContext(obj, name, method);
        rpcComponents.put(name, context);
    }

    @Override
    public boolean isMatcher(Annotation annotation) {
        return annotation.annotationType() == RpcService.class;
    }

    @Override
    public int getPriority() {
        return -1;
    }

}
