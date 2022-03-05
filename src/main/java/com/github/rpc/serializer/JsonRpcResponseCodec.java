package com.github.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.tinylog.Logger;

/**
 * @author Ray
 * @date created in 2022/3/5 11:18
 */
public class JsonRpcResponseCodec implements ObjectCodec<RpcResponse, ByteBuf> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public RpcResponse deserializer(ByteBuf source) throws Exception {
        String content = readTextContent(source);
        try {
            return mapper.readValue(content, RpcResponse.class);
        } catch (JsonProcessingException e) {
            Logger.error("parse {} json error, ex：{}", content, e.getMessage());
            throw e;
        }
    }

    @Override
    public ByteBuf serializer(RpcResponse source) throws Exception {
        try {
            byte[] data = mapper.writeValueAsBytes(source);
            return Unpooled.copiedBuffer(data);
        } catch (JsonProcessingException ex) {
            Logger.error("{} encode to json data failed, ex: {}", source, ex.getMessage());
            throw ex;
        }
    }

}
