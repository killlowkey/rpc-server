package com.github.rpc.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

/**
 * 注册实体
 *
 * @author Ray
 * @date created in 2022/5/26 10:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry {
    private String serviceName;
    private String methodName;
    private Method method;
    private InetSocketAddress address;
}
