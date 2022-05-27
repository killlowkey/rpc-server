package com.github.rpc.registry.memory;

import com.github.rpc.registry.Entry;
import com.github.rpc.registry.Registry;
import com.github.rpc.registry.RegistryConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/5/26 10:41
 */
public class MemoryRegistry implements Registry {

    protected final Map<String, List<Entry>> container = new HashMap<>();
    private final List<InetSocketAddress> addresses = new ArrayList<>();
    public static final String RPC_SERVER_ADDRESS = "rpc-server-address";

    public MemoryRegistry(RegistryConfig config) {
        List<InetSocketAddress> addressList = (List)config.get(RPC_SERVER_ADDRESS);
        if (addressList != null) {
            addresses.addAll(addressList);
        }
    }

    @Override
    public void register(Entry entry) {
        String serviceName = entry.getServiceName();
        List<Entry> entries = container.computeIfAbsent(serviceName, name -> new ArrayList<>());
        entries.add(entry);
    }

    @Override
    public void unregister(Entry entry) {
        String serviceName = entry.getServiceName();
        List<Entry> entries = container.computeIfAbsent(serviceName, name -> new ArrayList<>());
        entries.remove(entry);
    }

    @Override
    public Entry lookupEntry(String serviceName, String methodName) {
        List<Entry> entries = container.get(serviceName);
        if (entries == null) {
            return null;
        }

        for (Entry entry : entries) {
            if (entry.getMethodName().equals(methodName)) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return addresses;
    }

}
