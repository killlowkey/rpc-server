package com.github.rpc.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 订阅通知
 *
 * @author Ray
 * @date created in 2022/5/27 7:50
 */
public interface NotifyListener {

    /**
     * 有新订阅时，该方法会被调用
     */
    void notify(String serviceName, List<InetSocketAddress> addresses);

}
