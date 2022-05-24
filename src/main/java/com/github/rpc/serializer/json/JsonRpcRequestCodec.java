package com.github.rpc.serializer.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Json 请求编解码器
 *
 * @author Ray
 * @date created in 2022/5/23 20:13
 */
public class JsonRpcRequestCodec extends ByteToMessageCodec<JsonRpcRequest> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void encode(ChannelHandlerContext ctx,
                          JsonRpcRequest msg,
                          ByteBuf out) throws Exception {
        byte[] data = mapper.writeValueAsBytes(msg);
        out.writeBytes(data);
    }

    @Override
    public void decode(ChannelHandlerContext ctx,
                          ByteBuf in,
                          List<Object> out) throws Exception {
        int length = in.readableBytes();
        byte[] data = new byte[length];
        in.readBytes(data, 0, length);
        out.add(mapper.readValue(data, JsonRpcRequest.class));
    }

}
