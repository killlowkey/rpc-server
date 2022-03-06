package com.github.rpc.annotation;

import cn.hutool.core.util.ClassUtil;
import com.github.rpc.core.RpcServiceConfiguration;
import io.netty.util.internal.StringUtil;
import org.tinylog.Logger;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 注解扫描器
 * 1、每个注解应有自身的解析器
 * 2、所有的扫描结果应该添加到全局配置中
 *
 * @author Ray
 * @date created in 2022/3/4 7:31
 */
public class AnnotationScanner {

    private final String packageName;
    private static final List<AnnotationProcessor> PROCESSORS = new ArrayList<>();
    private final RpcServiceConfiguration configuration;
    private final Set<Class<?>> scanClass = new HashSet<>();

    static {
        PROCESSORS.add(new RateLimitAnnotationProcessor());
        PROCESSORS.add(new RpcServiceAnnotationProcessor());
        PROCESSORS.add(new AliasAnnotationProcessor());
        // 按照优先级排序，从小到大，小的优先级更高
        PROCESSORS.sort(Comparator.comparingInt(AnnotationProcessor::getPriority));
    }

    public AnnotationScanner(String packageName, RpcServiceConfiguration configuration) {
        this.packageName = packageName;
        this.configuration = configuration;
    }

    public void registerScanClass(Class<?>... clazz) {
        if (clazz == null) {
            return;
        }
        this.scanClass.addAll(Arrays.asList(clazz));
    }

    public void scan() {
        ScanContext.setConfiguration(configuration);

        Set<Class<?>> components = new HashSet<>(this.scanClass);
        // packName 不为空，扫描包下所有的类
        if (!StringUtil.isNullOrEmpty(this.packageName)) {
            Logger.info("scan {} package components", this.packageName);
            components.addAll(ClassUtil.scanPackage(this.packageName));
        }

        components.forEach(clazz -> {
            // 处理类注解
            processClass(clazz);
            // 处理方法注解
            Arrays.stream(clazz.getDeclaredMethods()).forEach(this::processMethod);
        });
    }

    private void processClass(Class<?> source) {
        // 遍历类上所有注解
        Arrays.stream(source.getAnnotations()).forEach(annotation -> {
            // 调用注解处理器
            PROCESSORS.forEach(processor -> {
                if (processor.isMatcher(annotation)) {
                    ScanContext context = new ScanContext(source);
                    processor.process(context, annotation);
                }
            });
        });
    }

    private void processMethod(Method source) {
        Arrays.stream(source.getAnnotations()).forEach(annotation -> {
            PROCESSORS.forEach(processor -> {
                if (processor.isMatcher(annotation)) {
                    ScanContext context = new ScanContext(source.getDeclaringClass(), source);
                    processor.process(context, annotation);
                }
            });
        });
    }

}

