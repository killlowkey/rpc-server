package com.github.rpc.example.rpc;

import com.github.rpc.annotation.RpcService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Ray
 * @date created in 2022/5/26 20:12
 */
@RpcService
public class OrderServiceImpl implements OrderService{
    @Override
    public List<String> getAllOrders() {
        return Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
}
