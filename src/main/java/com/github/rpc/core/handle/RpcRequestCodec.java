package com.github.rpc.core.handle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.rpc.constant.ErrorEnum;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import com.github.rpc.serializer.JsonRpcRequestCodec;
import com.github.rpc.serializer.JsonRpcResponseCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * rpc 请求编解码器
 *
 * @author Ray
 * @date created in 2022/3/5 10:44
 */
public class RpcRequestCodec extends ByteToMessageCodec<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestCodec.class);

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
            if (logger.isDebugEnabled()) {
                logger.debug("parse {} error, ex：{}", in.toString(), ex.getMessage());
            }

            // 写回客户端，因为该 Handler 前面没有响应编码器，所以需要手动编码
            RpcResponse response = new RpcResponse(ErrorEnum.PARSE_ERROR);
            ByteBuf byteBuf = new JsonRpcResponseCodec().serializer(response);
            ctx.writeAndFlush(byteBuf);
        }
    }
}
