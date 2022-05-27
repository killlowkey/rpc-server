package com.github.rpc.registry.zookeeper;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.github.rpc.registry.Entry;
import com.github.rpc.registry.Registry;
import com.github.rpc.registry.RegistryConfig;
import com.github.rpc.registry.memory.MemoryRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Zookeeper 注册中心
 *
 * @author Ray
 * @date created in 2022/5/26 10:38
 */
public class ZookeeperRegistry extends MemoryRegistry implements Registry {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private static final long UNKNOWN_SESSION_ID = -1L;
    public static final String ROOT = "rpc-server";
    public static final String ZOOKEEPER_KEY = "connectString";

    private final Set<String> persistentExistNodePath = new ConcurrentHashSet<>();
    private final CuratorFramework client;
    private long lastSessionId = UNKNOWN_SESSION_ID;


    public ZookeeperRegistry(RegistryConfig config) {
        super(config);

        String connectString = (String) config.get(ZOOKEEPER_KEY);
        if (connectString == null) {
            throw new IllegalArgumentException("not found zookeeper address in config");
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        // 连接监听器：连接服务端后注册
        client.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                long sessionId = getSessionId(client);
                if (sessionId == UNKNOWN_SESSION_ID) {
                    client = null;
                    return;
                }

                // 会话重连后，需要重新注册 entry
                if (sessionId != ZookeeperRegistry.this.lastSessionId) {
                    this.lastSessionId = sessionId;
                    recover();
                }
            }
        });
        client.start();
        this.lastSessionId = getSessionId(client);
    }

    @Override
    public void register(Entry entry) {
        checkDestroyed();
        super.register(entry);
        String path = buildPath(entry);
        // 节点存在无需注册
        if (checkExist(path)) {
            return;
        }
        doRegister(path, true);
    }

    /**
     * 递归创建节点
     */
    private void doRegister(String path, boolean ephemeral) {
        if (!ephemeral) {
            if (persistentExistNodePath.contains(path)) {
                return;
            }
            if (checkExist(path)) {
                persistentExistNodePath.add(path);
                return;
            }
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            doRegister(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
            persistentExistNodePath.add(path);
        }
    }


    @Override
    public void unregister(Entry entry) {
        // 从本地注册移除该 entry
        super.unregister(entry);
        checkDestroyed();

        List<Entry> entries = container.get(entry.getServiceName());
        // 当前服务下没有节点，可以直接删除
        if (entries == null || entries.isEmpty()) {
            String path = buildPath(entry);
            deletePath(path);
        }
    }

    private String buildPath(Entry entry) {
        InetSocketAddress socketAddress = entry.getAddress();
        String address = socketAddress.getHostString() + ":" + socketAddress.getPort();
        return String.format("/%s/%s/%s", ROOT, entry.getServiceName(), address);
    }

    @Override
    public Entry lookupEntry(String serviceName, String methodName) {
        return super.lookupEntry(serviceName, methodName);
    }

    /**
     * TODO 当前 RPC 功能很单一，暂时用不到，以后改成监视器方式
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        try {
            String path = "/" + ROOT + "/" + serviceName;
            List<String> addressList = client.getChildren().forPath(path);
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
            return new ArrayList<>();
        }
    }

    private void checkDestroyed() {
        if (client == null) {
            throw new IllegalStateException("registry is destroyed");
        }
    }

    private long getSessionId(CuratorFramework client) {
        try {
            return client.getZookeeperClient().getZooKeeper().getSessionId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void recover() {
        super.container.forEach((name, entries) -> entries.forEach(this::register));
    }

    private void createPersistent(String path) {
        try {
            // 当节点不存在时注册
            if (!checkExist(path)) {
                client.create().forPath(path);
                persistentExistNodePath.add(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkExist(String path) {
        if (persistentExistNodePath.contains(path)) {
            return true;
        }

        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void createEphemeral(String path) {
        checkDestroyed();
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
            log.info("ZNode {} already exists, In this case, we can just try to delete and create again.", path);
            deletePath(path);
            createEphemeral(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deletePath(String path) {
        try {
            persistentExistNodePath.remove(path);
            client.delete().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
