package com.github.rpc.registry.memory;

import com.github.rpc.registry.NotifyListener;
import com.github.rpc.registry.RegisterMode;
import com.github.rpc.registry.RegistryConfig;
import com.github.rpc.registry.ServiceDiscovery;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.rpc.registry.memory.MemoryRegistry.RPC_SERVER_ADDRESS;

/**
 * @author Ray
 * @date created in 2022/5/26 19:04
 */
public class MemoryServiceDiscovery implements ServiceDiscovery {

    private final List<InetSocketAddress> addresses = new ArrayList<>();
    private final Set<String> subscribeServices = new HashSet<>();


    public MemoryServiceDiscovery(RegistryConfig config) {
        List<InetSocketAddress> addressList = (List) config.get(RPC_SERVER_ADDRESS);
        if (addressList != null) {
            addresses.addAll(addressList);
        }
    }

    @Override
    public void subscribe(String serviceName, NotifyListener listener) {
        if (!subscribeServices.contains(serviceName)) {
            subscribeServices.add(serviceName);
            listener.notify(serviceName, lookup(serviceName));
        }
    }

    @Override
    public void unsubscribe(String serviceName) {
        subscribeServices.remove(serviceName);
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return this.addresses;
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(subscribeServices);
    }

    @Override
    public RegisterMode getMode() {
        return RegisterMode.MEMORY;
    }

}
