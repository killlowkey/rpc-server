package com.github.rpc.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.rpc.serializer.JsonRpcRequestCodec;
import com.github.rpc.serializer.JsonRpcResponseCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.tinylog.Logger;

import java.util.List;

/**
 * rpc 请求编解码器
 *
 * @author Ray
 * @date created in 2022/3/5 10:44
 */
public class RpcRequestCodecHandle extends ByteToMessageCodec<RpcRequest> {

    private final JsonRpcRequestCodec codec = new JsonRpcRequestCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf out) throws Exception {
        ByteBuf data = this.codec.serializer(msg);
        out.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            RpcRequest request = this.codec.deserializer(in);
            out.add(request);
        } catch (JsonProcessingException ex) {
            if (Logger.isDebugEnabled()) {
                Logger.debug("parse {} error, ex：{}", in.toString(), ex.getMessage());
            }

            // 写回客户端，因为该 Handler 前面没有响应编码器，所以需要手动编码
            RpcResponse.ErrorMsg errorMsg = new RpcResponse.ErrorMsg(-32700, "Parse error");
            RpcResponse rpcResponse = new RpcResponse(errorMsg);
            ByteBuf byteBuf = new JsonRpcResponseCodec().serializer(rpcResponse);
            ctx.writeAndFlush(byteBuf);
        }
    }
}
