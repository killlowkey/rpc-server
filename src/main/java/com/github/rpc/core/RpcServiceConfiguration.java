package com.github.rpc.core;

import com.github.rpc.annotation.RateLimitEntry;
import com.github.rpc.invoke.MethodContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ray
 * @date created in 2022/3/4 13:19
 */
@Data
public class RpcServiceConfiguration {

    private final Map<String, MethodContext> rpcComponents = new HashMap<>();
    private final Map<String, RateLimitEntry> rateLimitEntryMap = new HashMap<>();

}
