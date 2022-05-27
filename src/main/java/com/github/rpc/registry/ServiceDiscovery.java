package com.github.rpc.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Ray
 * @date created in 2022/5/26 17:41
 */
public interface ServiceDiscovery {

    /**
     * 订阅服务，注意订阅之前该服务必须存在
     */
    void subscribe(String serviceName, NotifyListener listener);

    /**
     * 解除订阅服务
     */
    void unsubscribe(String serviceName);

    /**
     * 获取服务地址
     */
    List<InetSocketAddress> lookup(String serviceName);

    /**
     * 获取所有注册服务
     */
    List<String> getServices();

    RegisterMode getMode();

}
