package com.github.rpc.loadbalance;

import com.github.rpc.RpcClient;

/**
 * 负载均衡
 *
 * @author Ray
 * @date created in 2022/3/6 13:36
 */
public interface LoadBalance {

    RpcClient select(String serviceName);

}
