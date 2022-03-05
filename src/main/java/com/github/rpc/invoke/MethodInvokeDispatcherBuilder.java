package com.github.rpc.invoke;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.asm.AsmGenerator;
import com.github.rpc.invoke.mh.MethodHandleMethodInvokeDispatcher;
import com.github.rpc.invoke.reflect.ReflectMethodInvokeDispatcher;
import com.github.rpc.utils.RpcUtil;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 1、注册 Rpc 组件
 * 2、收集调用监听器
 * 3、配置注解扫描器
 *
 * @author Ray
 * @date created in 2022/3/3 8:11
 */
public class MethodInvokeDispatcherBuilder {

    private InvokeType type = InvokeType.ASM;
    private final List<MethodInvokeListener> listeners = new ArrayList<>();
    private final RpcServiceConfiguration configuration;

    public MethodInvokeDispatcherBuilder(RpcServiceConfiguration configuration) {
        this.configuration = configuration;
    }


    public MethodInvokeDispatcherBuilder invokeType(InvokeType type) {
        this.type = type;
        return this;
    }


    public MethodInvokeDispatcherBuilder addInvokeListener(MethodInvokeListener listener) {
        if (listener == null) {
            return this;
        }

        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }

        return this;
    }

    public MethodInvokeDispatcherBuilder addInvokeListeners(List<MethodInvokeListener> listeners) {
        listeners.forEach(this::addInvokeListener);
        return this;
    }


    public MethodInvokeDispatcher build() {

        Map<String, MethodContext> rpcComponents = this.configuration.getRpcComponents();
        rpcComponents.forEach((name, context) -> {
            RpcUtil.registerMethod(name, context.getMethod());
            if (Logger.isDebugEnabled()) {
                Logger.debug("register [{}] rpc to server", name);
            }
        });

        switch (this.type) {
            case ASM:
                return new AsmGenerator(rpcComponents, listeners).generate();
            case REFLECT:
                return new ReflectMethodInvokeDispatcher(rpcComponents, listeners);
            case METHOD_HANDLE:
                return new MethodHandleMethodInvokeDispatcher(rpcComponents, listeners);
            default:
                throw new IllegalArgumentException(String.format("not found %s invokeType", this.type));
        }

    }

}
