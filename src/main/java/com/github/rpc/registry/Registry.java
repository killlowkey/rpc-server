package com.github.rpc.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 注册中心接口
 *
 * @author Ray
 * @date created in 2022/5/26 10:28
 */
public interface Registry {

    /**
     * 注册 rpc 方法
     *
     * @param entry rpc 方法实体
     */
    void register(Entry entry);

    /**
     * 解除注册
     */
    void unregister(Entry entry);

    /**
     * 从本地查找 entry
     */
    Entry lookupEntry(String serviceName, String methodName);

    /**
     * 从 Zookeeper 查找服务地址
     */
    List<InetSocketAddress> lookup(String serviceName);

}
