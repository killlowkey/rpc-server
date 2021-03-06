package com.github.rpc.invoke;

import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.invoke.asm.AsmGenerator;
import com.github.rpc.invoke.mh.MethodHandleMethodInvokeDispatcher;
import com.github.rpc.invoke.reflect.ReflectMethodInvokeDispatcher;
import com.github.rpc.registry.Entry;
import com.github.rpc.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(MethodInvokeDispatcherBuilder.class);

    private InvokeType type = InvokeType.ASM;
    private final List<MethodInvokeListener> listeners = new ArrayList<>();
    private final RpcServiceConfiguration configuration;
    private boolean saveAsmByteCode;

    public MethodInvokeDispatcherBuilder(RpcServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    public MethodInvokeDispatcherBuilder invokeType(InvokeType type) {
        this.type = type;
        return this;
    }

    public MethodInvokeDispatcherBuilder enableSaveAsmByteCode() {
        this.saveAsmByteCode = true;
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
        Registry registry = this.configuration.getRegistry();
        // 服务注册
        rpcComponents.forEach((name, context) -> {
            String[] names = name.split("#");
            if (names.length != 2) {
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Register [{}] RPC", name);
            }

            Entry entry = new Entry(names[0], names[1], context.getMethod(), configuration.getAddress());
            registry.register(entry);
        });

        switch (this.type) {
            case ASM:
                return new AsmGenerator(rpcComponents, listeners, this.saveAsmByteCode).generate();
            case REFLECT:
                return new ReflectMethodInvokeDispatcher(rpcComponents, listeners);
            case METHOD_HANDLE:
                return new MethodHandleMethodInvokeDispatcher(rpcComponents, listeners);
            default:
                throw new IllegalArgumentException(String.format("not found %s invokeType", this.type));
        }

    }

}
