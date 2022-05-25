package com.github.rpc.serializer.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Json 响应编解码器
 *
 * @author Ray
 * @date created in 2022/5/23 20:15
 */
public class JsonRpcResponseCodec extends ByteToMessageCodec<JsonRpcResponse> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          JsonRpcResponse msg,
                          ByteBuf out) throws Exception {
        byte[] data = mapper.writeValueAsBytes(msg);
        out.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in,
                          List<Object> out) throws Exception {
        int length = in.readableBytes();
        byte[] data = new byte[length];
        in.readBytes(data, 0, length);
        out.add(mapper.readValue(data, JsonRpcResponse.class));
    }
}
