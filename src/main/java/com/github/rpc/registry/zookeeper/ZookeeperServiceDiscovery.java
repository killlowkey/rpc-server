package com.github.rpc.registry.zookeeper;

import com.github.rpc.registry.NotifyListener;
import com.github.rpc.registry.RegisterMode;
import com.github.rpc.registry.RegistryConfig;
import com.github.rpc.registry.ServiceDiscovery;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.rpc.registry.zookeeper.ZookeeperRegistry.ROOT;
import static com.github.rpc.registry.zookeeper.ZookeeperRegistry.ZOOKEEPER_KEY;

/**
 * Zookeeper 服务发现
 *
 * @author Ray
 * @date created in 2022/5/26 18:07
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private final CuratorFramework client;
    private final Set<String> subscribeServices = new HashSet<>();
    private final Map<String, Set<InetSocketAddress>> services = new ConcurrentHashMap<>();
    private final Map<String, NotifyListener> listeners = new ConcurrentHashMap<>();
    private final Watcher watcher = new SubScribeWatcher();

    public ZookeeperServiceDiscovery(RegistryConfig config) {
        String connectString = (String) config.get(ZOOKEEPER_KEY);
        if (connectString == null) {
            throw new IllegalArgumentException("not found zookeeper address in config");
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();
    }

    @Override
    public void subscribe(String serviceName, NotifyListener listener) {
        try {
            // 已经订阅了则无需订阅
            if (subscribeServices.contains(serviceName)) {
                return;
            }

            // 添加监听器
            listeners.put(serviceName, listener);

            // 首次订阅需要通知
            listener.notify(serviceName, lookup(serviceName));

            // 构建 znode 路径
            String path = buildPath(serviceName);
            subscribeServices.add(path);
            List<InetSocketAddress> addresses = getChildren(path, true);
            services.computeIfAbsent(path, name -> new HashSet<>()).addAll(addresses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unsubscribe(String serviceName) {
        String path = buildPath(serviceName);
        subscribeServices.remove(path);
    }

    private List<InetSocketAddress> getChildren(String path, boolean watch) {
        GetChildrenBuilder children = client.getChildren();
        if (watch) {
            children.usingWatcher(watcher);
        }
        try {
            List<String> addressList = children.forPath(path);
            return addressList.stream().map(address -> {
                // ipv4 127.0.0.1:8080
                String[] texts = address.split(":");
                if (texts.length != 2) {
                    return null;
                }
                try {
                    InetAddress inetAddress = InetAddress.getByName(texts[0]);
                    return new InetSocketAddress(inetAddress, Integer.parseInt(texts[1]));
                } catch (UnknownHostException e) {
                    return null;
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        String path = buildPath(serviceName);
        Set<InetSocketAddress> socketAddresses = services.computeIfAbsent(path, name -> new HashSet<>());
        if (socketAddresses.isEmpty()) {
            socketAddresses.addAll(getChildren(path, true));
        }
        return new ArrayList<>(socketAddresses);
    }

    @Override
    public List<String> getServices() {
        try {
            return client.getChildren().forPath("/" + ROOT);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public RegisterMode getMode() {
        return RegisterMode.ZOOKEEPER;
    }

    private String buildPath(String serviceName) {
        return "/" + ROOT + "/" + serviceName;
    }

    class SubScribeWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() != Event.EventType.NodeChildrenChanged) {
                return;
            }

            String path = event.getPath();
            if (subscribeServices.contains(path)) {
                try {
                    List<InetSocketAddress> children = getChildren(path, true);
                    services.computeIfAbsent(path, name -> new HashSet<>()).addAll(children);
                    // 通知监听器
                    String serviceName = path.substring(path.lastIndexOf("/") + 1);
                    listeners.get(serviceName).notify(serviceName, lookup(serviceName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
