package com.github.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.DefaultRpcResponse;
import com.github.rpc.core.Metadata;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.serializer.json.JsonRpcRequest;
import com.github.rpc.serializer.json.JsonRpcRequestCodec;
import com.github.rpc.serializer.json.JsonRpcResponse;
import com.github.rpc.serializer.json.JsonRpcResponseCodec;
import com.github.rpc.serializer.protobuf.PbRpcRequest;
import com.github.rpc.serializer.protobuf.PbRpcRequestDecoder;
import com.github.rpc.serializer.protobuf.PbRpcResponse;
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
 * 序列化测试
 *
 * @author Ray
 * @date created in 2022/5/23 21:11
 */
@RunWith(JUnit4.class)
public class ChannelHandlerTest {


    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void jsonTest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new JsonObjectDecoder(),
                new JsonRpcRequestCodec(),
                new JsonRpcResponseCodec(),
                new MessageConvertHandler(Serializer.JSON)
        );

        // ============= 测试入站数据 =============
        JsonRpcRequest request = new JsonRpcRequest("1", "say", new Object[]{"ray", 10}, new Metadata());
        byte[] data = mapper.writeValueAsBytes(request);
        ByteBuf byteBuf = Unpooled.copiedBuffer(data);
        channel.writeInbound(byteBuf);
//        channel.finish();

        RpcRequest rpcRequest = channel.readInbound();
        System.out.println(rpcRequest);
        Assert.assertEquals("1", rpcRequest.getId());
        Assert.assertEquals("say", rpcRequest.getName());
        Assert.assertArrayEquals(new Object[]{"ray", 10}, rpcRequest.getParams());


        // =========== 测试出站数据 =============
        DefaultRpcResponse response = new DefaultRpcResponse("1", 200, "success", 100, new Metadata());
        channel.writeOutbound(response);
        channel.finish();

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse("1", 200, "success", 100, new Metadata());
        byte[] responseData = mapper.writeValueAsBytes(jsonRpcResponse);

        // 读取出站编码后数据
        ByteBuf buf = channel.readOutbound();
        byte[] encodeData = new byte[buf.readableBytes()];
        buf.readBytes(encodeData, 0, encodeData.length);
        Assert.assertArrayEquals(responseData, encodeData);
    }

    @Test
    public void pbTest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new ProtobufVarint32FrameDecoder(),
                new PbRpcRequestDecoder(),
                new ProtobufVarint32LengthFieldPrepender(),
                new ProtobufEncoder(),
                new MessageConvertHandler(Serializer.PROTOBUF)
        );

        // ============= 测试入站消息 =============
        Object[] params = new Object[]{"ray", 10};
        PbRpcRequest request = PbRpcRequest.newBuilder()
                .setId("1")
                .setName("say")
                .setParams(ByteString.copyFrom(mapper.writeValueAsBytes(params)))
                .build();
        // 适配 ProtobufVarint32LengthFieldPrepender
        ByteBuf byteBuf = Unpooled.copiedBuffer(request.toByteArray());
        ProtobufVarint32LengthFieldPrependerImpl prepender = new ProtobufVarint32LengthFieldPrependerImpl();
        ByteBuf dest = Unpooled.buffer();
        prepender.encode(null, byteBuf, dest);
        // 写入入站数据
        channel.writeInbound(dest);
//        channel.finish();
        // 读取入站数据
        RpcRequest rpcRequest = channel.readInbound();
        System.out.println(rpcRequest);
        Assert.assertEquals("1", rpcRequest.getId());
        Assert.assertEquals("say", rpcRequest.getName());
        Assert.assertArrayEquals(new Object[]{"ray", 10}, rpcRequest.getParams());


        // ============= 测试出站信息 =============
        DefaultRpcResponse response = new DefaultRpcResponse("1", 200, "success", 100, new Metadata());
        channel.writeOutbound(response);
        channel.finish();

        byte[] data = mapper.writeValueAsBytes(100);
        PbRpcResponse pbRpcResponse = PbRpcResponse.newBuilder()
                .setId("1")
                .setCode(200)
                .setMessage("success")
                .setResult(ByteString.copyFrom(data))
                .build();
        ByteBuf byteBuf2 = Unpooled.copiedBuffer(pbRpcResponse.toByteArray());
        ProtobufVarint32LengthFieldPrependerImpl prepender2 = new ProtobufVarint32LengthFieldPrependerImpl();
        ByteBuf dest2 = Unpooled.buffer();
        prepender2.encode(null, byteBuf2, dest2);

        // 期待数据
        byte[] responseData = new byte[dest2.readableBytes()];
        dest2.readBytes(responseData, 0, responseData.length);
        // 出站数据
        ByteBuf buf = channel.readOutbound();
        byte[] encodeData = new byte[buf.readableBytes()];
        buf.readBytes(encodeData, 0, encodeData.length);

        Assert.assertArrayEquals(responseData, encodeData);
    }


    @Test
    public void jsonOutboundTest() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new MessageConvertHandler(Serializer.JSON)
        );
        DefaultRpcResponse response = new DefaultRpcResponse("1", 200, "success", 100, new Metadata());
        channel.writeOutbound(response);
        channel.finish();

        JsonRpcResponse rpcResponse = channel.readOutbound();
        System.out.println(rpcResponse);
        Assert.assertEquals("1", rpcResponse.getId());
        Assert.assertEquals(200, rpcResponse.getCode());
        Assert.assertEquals("success", rpcResponse.getMessage());
        Assert.assertEquals(100, rpcResponse.getResult());
    }

    @Test
    public void pbOutboundTest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new MessageConvertHandler(Serializer.PROTOBUF)
        );
        DefaultRpcResponse response = new DefaultRpcResponse("1", 200, "success", 100, new Metadata());
        channel.writeOutbound(response);
        channel.finish();

        PbRpcResponse rpcResponse = channel.readOutbound();
        System.out.println(rpcResponse);
        Assert.assertEquals("1", rpcResponse.getId());
        Assert.assertEquals(200, rpcResponse.getCode());
        Assert.assertEquals("success", rpcResponse.getMessage());

        ObjectMapper mapper = new ObjectMapper();
        ByteString result = rpcResponse.getResult();
        int value = mapper.readValue(result.toByteArray(), int.class);
        Assert.assertEquals(100, value);
    }

    public static final class ProtobufVarint32LengthFieldPrependerImpl extends ProtobufVarint32LengthFieldPrepender {
        @Override
        public void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
