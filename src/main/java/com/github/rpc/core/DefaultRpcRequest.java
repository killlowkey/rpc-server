package com.github.rpc.core;

import com.github.rpc.core.RpcRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 底层 RPC 实现
 *
 * @author Ray
 * @date created in 2022/5/23 20:41
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DefaultRpcRequest implements RpcRequest {
    private String id;
    private String name;
    private Object[] params;
    private Metadata metadata;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object[] getParams() {
        return this.params;
    }

    @Override
    public Metadata getMetadata() {
        return this.metadata;
    }

}
