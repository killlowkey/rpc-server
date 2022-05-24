package com.github.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.DefaultRpcResponse;
import com.github.rpc.core.Metadata;
import com.github.rpc.serializer.json.JsonRpcRequestCodec;
import com.github.rpc.serializer.json.JsonRpcResponse;
import com.github.rpc.serializer.json.JsonRpcResponseCodec;
import com.github.rpc.serializer.protobuf.PbRpcResponse;
import com.github.rpc.serializer.protobuf.PbRpcResponseDecoder;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ray
 * @date created in 2022/5/24 9:50
 */
@RunWith(JUnit4.class)
public class ClientTest {
    @Test
    public void JsonTest() throws Exception {

        JsonRpcResponse response = new JsonRpcResponse("0", 200, "success", 100, new Metadata());

        EmbeddedChannel channel = new EmbeddedChannel(
                new JsonObjectDecoder(),
                new JsonRpcResponseCodec(),
                new JsonRpcRequestCodec(),
                new MessageConvertHandler(Serializer.JSON)
        );

        ObjectMapper mapper = new ObjectMapper();
        ByteBuf buf = Unpooled.copiedBuffer(mapper.writeValueAsBytes(response));
        channel.writeInbound(buf);
        channel.finish();

        DefaultRpcResponse defaultRpcResponse  = channel.readInbound();
        System.out.println(defaultRpcResponse);
        Assert.assertNotNull(defaultRpcResponse);
    }

    @Test
    public void pbTest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new ProtobufVarint32FrameDecoder(),
                new PbRpcResponseDecoder(),
                new ProtobufVarint32LengthFieldPrepender(),
                new ProtobufEncoder(),
                new MessageConvertHandler(Serializer.JSON)
        );

        ObjectMapper mapper = new ObjectMapper();
        PbRpcResponse pbRpcResponse = PbRpcResponse.newBuilder()
                .setId("0")
                .setCode(200)
                .setMessage("success")
                .setResult(ByteString.copyFrom(mapper.writeValueAsBytes(null)))
                .build();
        byte[] data = pbRpcResponse.toByteArray();
        ProtobufVarint32LengthFieldPrependerImpl protobufVarint32LengthFieldPrepender =
                new ProtobufVarint32LengthFieldPrependerImpl();
        ByteBuf buffer = Unpooled.buffer();
        protobufVarint32LengthFieldPrepender.encode(null, Unpooled.copiedBuffer(data), buffer);

        channel.writeInbound(buffer);
        channel.finish();
        Object o = channel.readInbound();
        System.out.println(o.getClass());
    }

    public static final class ProtobufVarint32LengthFieldPrependerImpl extends ProtobufVarint32LengthFieldPrepender {
        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
