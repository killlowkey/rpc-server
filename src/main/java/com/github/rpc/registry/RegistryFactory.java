package com.github.rpc.registry;

import com.github.rpc.registry.memory.MemoryRegistry;
import com.github.rpc.registry.zookeeper.ZookeeperRegistry;

/**
 * 注册中心工厂
 *
 * @author Ray
 * @date created in 2022/5/26 10:34
 */
public class RegistryFactory {

    public static Registry getRegistry(RegisterMode mode, RegistryConfig config) {
        if (mode == RegisterMode.MEMORY) {
            return new MemoryRegistry(config);
        } else if (mode == RegisterMode.ZOOKEEPER) {
            return new ZookeeperRegistry(config);
        } else {
            String msg = String.format("not found %s mode", mode);
            throw new IllegalArgumentException(msg);
        }
    }

}
