package com.github.rpc.example.rpc;

import com.github.rpc.annotation.RpcClient;

import java.util.List;

/**
 * @author Ray
 * @date created in 2022/5/26 20:12
 */
@RpcClient
public interface OrderService {
    List<String> getAllOrders();
}
