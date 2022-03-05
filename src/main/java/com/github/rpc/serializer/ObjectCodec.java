package com.github.rpc.serializer;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 编解码器
 *
 * @author Ray
 * @date created in 2022/3/5 11:03
 */
public interface ObjectCodec<S, T> {

    S deserializer(T source) throws Exception;
    T serializer(S source) throws Exception;

    default String readTextContent(ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        if (length == 0) {
            return "";
        }

        byte[] data = new byte[length];
        byteBuf.readBytes(data, 0, length);
        return new String(data, StandardCharsets.UTF_8);
    }
}
