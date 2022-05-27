package com.github.rpc.registry;

import com.github.rpc.registry.memory.MemoryServiceDiscovery;
import com.github.rpc.registry.zookeeper.ZookeeperServiceDiscovery;

/**
 * @author Ray
 * @date created in 2022/5/26 20:32
 */
public class ServiceDiscoveryFactory {

    public static ServiceDiscovery getServiceDiscovery(RegisterMode mode,
                                                       RegistryConfig config) {
        if (mode == RegisterMode.MEMORY) {
            return new MemoryServiceDiscovery(config);
        } else if (mode == RegisterMode.ZOOKEEPER) {
            return new ZookeeperServiceDiscovery(config);
        } else {
            return new MemoryServiceDiscovery(config);
        }
    }
}
