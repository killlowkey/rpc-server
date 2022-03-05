package com.github.rpc.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.rpc.serializer.JsonRpcResponseCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.tinylog.Logger;

import java.util.List;

/**
 * 编码与解码 rpc 响应
 * <p>
 * client decode：byte[] -> rpc response
 * server encode: rpc response -> byte[]
 *
 * @author Ray
 * @date created in 2022/3/5 10:44
 */
public class RpcResponseCodecHandle extends ByteToMessageCodec<RpcResponse> {

    private final JsonRpcResponseCodec codec = new JsonRpcResponseCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse msg, ByteBuf out) throws Exception {
        ByteBuf data = this.codec.serializer(msg);
        out.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            RpcResponse response = this.codec.deserializer(in);
            out.add(response);
        } catch (JsonProcessingException ex) {
            if (Logger.isDebugEnabled()) {
                Logger.debug("parse {} error, ex：{}", in.toString(), ex.getMessage());
            }
        }
    }
}
