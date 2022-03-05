package com.github.rpc.annotation;

import com.github.rpc.core.RpcServiceConfiguration;
import lombok.Data;
import org.tinylog.Logger;

import java.lang.reflect.Method;

/**
 * @author Ray
 * @date created in 2022/3/4 12:48
 */
@Data
public class ScanContext {

    // 需要加上配置类信息
    private Class<?> clazz;
    private Method method;
    private static RpcServiceConfiguration configuration;

    public ScanContext(Class<?> clazz) {
        this.clazz = clazz;
    }

    public ScanContext(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public static void setConfiguration(RpcServiceConfiguration configuration) {
        ScanContext.configuration = configuration;
    }

    public RpcServiceConfiguration getConfiguration() {
        if (configuration == null) {
            Logger.error("configuration is null in ScanContext");
            throw new RuntimeException("configuration is null");
        }
        return configuration;
    }

}
