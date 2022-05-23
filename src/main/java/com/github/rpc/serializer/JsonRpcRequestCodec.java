package com.github.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ray
 * @date created in 2022/3/5 10:10
 */
public class JsonRpcRequestCodec implements ObjectCodec<RpcRequest, ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcRequestCodec.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public RpcRequest deserializer(ByteBuf source) throws JsonProcessingException {
        String content = readTextContent(source);
        try {
            return mapper.readValue(content, RpcRequest.class);
        } catch (JsonProcessingException e) {
            logger.error("parse {} json error, ex：{}", content, e.getMessage());
            throw e;
        }
    }

    @Override
    public ByteBuf serializer(RpcRequest source) throws JsonProcessingException {
        try {
            byte[] data = mapper.writeValueAsBytes(source);
            return Unpooled.copiedBuffer(data);
        } catch (JsonProcessingException ex) {
            logger.error("{} encode to json data failed, ex: {}", source, ex.getMessage());
            throw ex;
        }
    }


}
