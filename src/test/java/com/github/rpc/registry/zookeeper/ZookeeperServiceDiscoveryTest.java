package com.github.rpc.registry.zookeeper;

import cn.hutool.core.net.NetUtil;
import com.github.rpc.registry.Entry;
import com.github.rpc.registry.NotifyListener;
import com.github.rpc.registry.RegistryConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ray
 * @date created in 2022/5/26 18:21
 */
@RunWith(JUnit4.class)
public class ZookeeperServiceDiscoveryTest extends TestCase {

    ZookeeperServiceDiscovery discovery;
    ZookeeperRegistry registry;

    @Before
    public void init() {
        RegistryConfig config = new RegistryConfig();
        config.put(ZookeeperRegistry.ZOOKEEPER_KEY, "106.55.54.202:2181");
        discovery = new ZookeeperServiceDiscovery(config);
        registry = new ZookeeperRegistry(config);
    }

    @Test
    public void testSubscribe() throws InterruptedException {
        String name = this.getClass().getName();
        // 订阅服务，需要先创建该节点，否则无法订阅
        discovery.subscribe(name, (n, addresses) -> {

        });

        List<InetSocketAddress> addressList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            InetSocketAddress address = NetUtil.createAddress("127.0.0.1", i);
            addressList.add(address);

            Entry entry = new Entry(name, "say" + i, null, address);
            registry.register(entry);
        }

        // 等待订阅处理完
        Thread.sleep(2000L);
        assertEquals(addressList, discovery.lookup(name));
    }
}