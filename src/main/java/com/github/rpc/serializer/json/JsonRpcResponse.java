package com.github.rpc.serializer.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.rpc.core.DefaultRpcResponse;
import com.github.rpc.core.Metadata;
import com.github.rpc.core.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ray
 * @date created in 2022/5/23 20:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcResponse {
    String id;
    int code;
    String message;
    Object result;
    Metadata metadata;

    public static JsonRpcResponse rpcResponseTo(RpcResponse response) {
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse();
        if (response == null) {
            return jsonRpcResponse;
        }

        jsonRpcResponse.setId(response.getId());
        jsonRpcResponse.setCode(response.getCode());
        jsonRpcResponse.setMessage(response.getMessage());
        jsonRpcResponse.setResult(response.getResult());
        jsonRpcResponse.setMetadata(response.getMetadata());
        return jsonRpcResponse;
    }

    public static RpcResponse jsonRpcResponseTo(JsonRpcResponse response) {
        DefaultRpcResponse rpcResponse = new DefaultRpcResponse();
        if (response == null) {
            return rpcResponse;
        }

        rpcResponse.setId(response.getId());
        rpcResponse.setCode(response.getCode());
        rpcResponse.setMessage(response.getMessage());
        rpcResponse.setResult(response.getResult());
        rpcResponse.setMetadata(response.getMetadata());
        return rpcResponse;
    }
}
