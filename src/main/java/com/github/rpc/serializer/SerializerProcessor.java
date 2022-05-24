package com.github.rpc.serializer;

import com.github.rpc.core.NettyProcessor;
import com.github.rpc.serializer.json.JsonRpcRequestCodec;
import com.github.rpc.serializer.json.JsonRpcResponseCodec;
import com.github.rpc.serializer.protobuf.PbRpcRequestDecoder;
import com.github.rpc.serializer.protobuf.PbRpcResponseDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Ray
 * @date created in 2022/5/23 21:33
 */
public class SerializerProcessor implements NettyProcessor {

    private final Serializer serializer;
    private final boolean client;

    public SerializerProcessor(Serializer serializer, boolean client) {
        this.serializer = serializer;
        this.client = client;
    }

    @Override
    public void processChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        switch (serializer) {
            case JSON:
                // 客户端先编码响应，在编码请求
                if (client) {
                    pipeline.addLast(new JsonObjectDecoder())
                            .addLast(new JsonRpcResponseCodec())
                            .addLast(new JsonRpcRequestCodec());
                } else {
                    pipeline.addLast(new JsonRpcRequestCodec())
                            .addLast(new JsonRpcResponseCodec());
                }
                break;
            case PROTOBUF:
                if (client) {
                    pipeline
                            .addLast(new ProtobufVarint32FrameDecoder())
                            // 响应解码
                            .addLast(new PbRpcResponseDecoder())
                            .addLast(new ProtobufVarint32LengthFieldPrepender())
                            .addLast(new ProtobufEncoder());
                } else {
                    pipeline
                            .addLast(new ProtobufVarint32FrameDecoder())
                            // 请求解码
                            .addLast(new PbRpcRequestDecoder())
                            .addLast(new ProtobufVarint32LengthFieldPrepender())
                            .addLast(new ProtobufEncoder());
                }
                break;
        }

        pipeline.addLast(new MessageConvertHandler(this.serializer));
    }
}
