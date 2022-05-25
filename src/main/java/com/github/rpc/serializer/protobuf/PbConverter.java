package com.github.rpc.serializer.protobuf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 构建 Pb 请求/响应 字段不允许有空值，会导致无法构建
 * 从而 Netty 出/入站没有数据
 *
 * https://stackoverflow.com/questions/29170183/how-to-set-repeated-fields-in-protobuf-before-building-the-message
 *
 * @author Ray
 * @date created in 2022/5/24 19:55
 */
public class PbConverter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static PbRpcRequest rpcToPbRequest(RpcRequest request) throws JsonProcessingException {
        if (request == null) {
            return PbRpcRequest.newBuilder().build();
        }

        // 请求参数
        byte[] data = mapper.writeValueAsBytes(request.getParams());
        PbRpcRequest.Builder builder = PbRpcRequest.newBuilder()
                .setId(request.getId())
                .setName(request.getName())
                .setParams(ByteString.copyFrom(data));

        // 构建 Metadata
        List<PbMetadata> metadataList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : request.getMetadata().entrySet()) {
            byte[] value = mapper.writeValueAsBytes(entry.getValue());
            PbMetadata metadata = PbMetadata.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(ByteString.copyFrom(value))
                    .build();
            metadataList.add(metadata);
        }
        builder.addAllMetadata(metadataList);

        return builder.build();
    }

    public static RpcRequest pbRequestToRpc(PbRpcRequest request) throws IOException {
        DefaultRpcRequest rpcRequest = new DefaultRpcRequest();
        if (request == null) {
            return rpcRequest;
        }

        rpcRequest.setId(request.getId());
        rpcRequest.setName(request.getName());
        // 方法参数
        Object[] params = mapper.readValue(request.getParams().toByteArray(), Object[].class);
        rpcRequest.setParams(params);

        // 元数据
        Metadata metadata = new Metadata();
        for (PbMetadata pbMetadata : request.getMetadataList()) {
            Object value = mapper.readValue(pbMetadata.getValue().toByteArray(), Object.class);
            metadata.put(pbMetadata.getKey(), value);
        }
        rpcRequest.setMetadata(metadata);

        return rpcRequest;
    }


    public static PbRpcResponse rpcToPbResponse(RpcResponse response) throws JsonProcessingException {
        if (response == null) {
            return PbRpcResponse.newBuilder().build();
        }

        PbRpcResponse.Builder builder = PbRpcResponse.newBuilder()
                .setId(response.getId())
                .setCode(response.getCode())
                .setMessage(response.getMessage());


        // 方法调用结果
        byte[] result = mapper.writeValueAsBytes(response.getResult());
        builder.setResult(ByteString.copyFrom(result));

        if (response.getMetadata() == null) {
            return builder.build();
        }

        // 构建元数据
        List<PbMetadata> metadataList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : response.getMetadata().entrySet()) {
            byte[] value = mapper.writeValueAsBytes(entry.getValue());
            PbMetadata metadata = PbMetadata.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(ByteString.copyFrom(value))
                    .build();
            metadataList.add(metadata);
        }
        builder.addAllMetadata(metadataList);

        return builder.build();
    }

    public static RpcResponse pbResponseToRpc(PbRpcResponse response) throws IOException {
        DefaultRpcResponse rpcResponse = new DefaultRpcResponse();
        if (response == null) {
            return rpcResponse;
        }

        rpcResponse.setId(response.getId());
        rpcResponse.setCode(response.getCode());
        rpcResponse.setMessage(response.getMessage());

        // 设置调用结果
        Object result = mapper.readValue(response.getResult().toByteArray(), Object.class);
        rpcResponse.setResult(result);

        // 元数据
        Metadata metadata = new Metadata();
        for (PbMetadata pbMetadata : response.getMetadataList()) {
            Object value = mapper.readValue(pbMetadata.getValue().toByteArray(), Object.class);
            metadata.put(pbMetadata.getKey(), value);
        }
        rpcResponse.setMetadata(metadata);

        return rpcResponse;
    }

}
