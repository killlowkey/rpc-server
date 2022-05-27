package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.MethodContext;
import com.github.rpc.utils.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ray
 * @date created in 2022/3/4 12:51
 */
public class RpcServiceAnnotationProcessor implements AnnotationProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceAnnotationProcessor.class);

    private RpcServiceConfiguration configuration;

    @Override
    public void process(ScanContext context, Annotation annotation) {
        if (logger.isDebugEnabled()) {
            logger.debug("process [{}] class @RpcService annotation",
                    context.getClazz().getSimpleName());
        }

        RpcService rpcService = (RpcService) annotation;
        this.configuration = context.getConfiguration();
        if (this.configuration == null) {
            logger.error("configuration is null, RpcServiceAnnotationProcessor execute failed");
            throw new RuntimeException("configuration is null");
        }

        Class<?> clazz = context.getClazz();
        Object obj = newInstance(clazz);

        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            if (MethodUtil.filterMethod(method)) {
                return;
            }

            String nmae = rpcService.value();
            // 为空则，采用方法所在的接口名
            nmae = Objects.equals(nmae, "") ? getInterfaceName(method) : nmae;
            // 注册 RPC 组件
            registerRpc(nmae, obj, method);
        });
    }

    private String getInterfaceName(Method method) {
        Class<?> aClass = method.getDeclaringClass();
        for (Class<?> anInterface : aClass.getInterfaces()) {
            for (Method m : anInterface.getDeclaredMethods()) {
                if (method.getName().equals(m.getName())
                        && method.getReturnType().equals(m.getReturnType())
                        && Arrays.equals(method.getParameterTypes(), m.getParameterTypes())) {
                    return anInterface.getName();
                }
            }
        }

        return "";
    }

    private Object newInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("not found {} default constructor", clazz.getSimpleName());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("instance {} object failed  ", clazz.getSimpleName());
            e.printStackTrace();
        }

        return null;
    }

    private void registerRpc(String parentName, Object obj, Method method) {
        Map<String, MethodContext> rpcComponents = configuration.getRpcComponents();
        // com.github.PersonService#say
        String name = parentName + "#" + method.getName();
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
