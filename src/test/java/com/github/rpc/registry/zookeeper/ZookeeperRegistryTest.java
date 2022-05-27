package com.github.rpc.registry.zookeeper;

import cn.hutool.core.net.NetUtil;
import com.github.rpc.registry.Entry;
import com.github.rpc.registry.RegistryConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

/**
 * @author Ray
 * @date created in 2022/5/26 17:06
 */
@RunWith(JUnit4.class)
public class ZookeeperRegistryTest extends TestCase {

    ZookeeperRegistry registry;

    @Before
    public void  init() {
        RegistryConfig config = new RegistryConfig();
        config.put(ZookeeperRegistry.ZOOKEEPER_KEY, "106.55.54.202:2181");
        registry = new ZookeeperRegistry(config);
    }

    @Test
    public void testRegister() throws InterruptedException {
        String serviceName = this.getClass().getName();
        InetSocketAddress address = new InetSocketAddress(NetUtil.getLocalhost(), 8080);

        Entry entry1 = new Entry(serviceName, "say", null, address);
        registry.register(entry1);
        assertNotNull(registry.lookupEntry(serviceName, "say"));

        assertEquals(address, registry.lookup(serviceName).get(0));
    }
}