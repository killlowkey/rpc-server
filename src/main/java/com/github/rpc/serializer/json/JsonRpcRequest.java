package com.github.rpc.serializer.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.rpc.core.DefaultRpcRequest;
import com.github.rpc.core.Metadata;
import com.github.rpc.core.RpcRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ray
 * @date created in 2022/5/23 19:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcRequest {
    private String id;
    private String name;
    private Object[] params;
    private Metadata metadata;

    /**
     * RpcRequest 转 JsonRpcRequest
     */
    public static JsonRpcRequest rpcRequestTo(RpcRequest request) {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        if (request == null) {
            return jsonRpcRequest;
        }

        jsonRpcRequest.setId(request.getId());
        jsonRpcRequest.setName(request.getName());
        jsonRpcRequest.setParams(request.getParams());
        jsonRpcRequest.setMetadata(request.getMetadata());
        return jsonRpcRequest;
    }

    /**
     * JsonRpcRequest 转 RpcRequest
     */
    public static RpcRequest jsonRpcRequestTo(JsonRpcRequest request) {
        DefaultRpcRequest rpcRequest = new DefaultRpcRequest();
        if (request == null) {
            return rpcRequest;
        }

        rpcRequest.setId(request.getId());
        rpcRequest.setName(request.getName());
        rpcRequest.setParams(request.getParams());
        rpcRequest.setMetadata(request.getMetadata());
        return rpcRequest;
    }

}
