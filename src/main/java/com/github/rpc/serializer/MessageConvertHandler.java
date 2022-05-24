package com.github.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.DefaultRpcRequest;
import com.github.rpc.core.DefaultRpcResponse;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.core.RpcResponse;
import com.github.rpc.serializer.json.JsonRpcRequest;
import com.github.rpc.serializer.json.JsonRpcResponse;
import com.github.rpc.serializer.protobuf.PbRpcRequest;
import com.github.rpc.serializer.protobuf.PbRpcResponse;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 服务端
 * JsonRpcRequest -----
 *                    | ----- decode handler(入站) -----> DefaultRpcRequest
 * PbRpcRequest  ----
 *
 *                                                         --------> JsonRpcResponse
 * DefaultRpcResponse   ----- encode handler（出站）-----> |
 *                                                        -------->  PbRpcRequest
 *
 * 客户端
 * JsonRpcResponse -----
 *                     | ----- decode handler(入站) -----> DefaultRpcResponse
 * PbRpcResponse   ----
 *
 *                                                          --------> JsonRpcRequest
 * DefaultRpcRequest     ----- encode handler（出站）-----> |
 *                                                         -------->  PbRpcRequest
 *
 * @author Ray
 * @date created in 2022/5/23 20:35
 */
public class MessageConvertHandler extends MessageToMessageCodec<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(MessageConvertHandler.class);

    private final Serializer serializer;

    public MessageConvertHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          Object msg,
                          List<Object> out) throws Exception {
        // 请求编码
        if (msg instanceof RpcRequest) {
            RpcRequest rpcRequest = (RpcRequest) msg;
            switch (serializer) {
                case JSON:
                    JsonRpcRequest jsonRpcRequest = JsonRpcRequest.rpcRequestTo(rpcRequest);
                    out.add(jsonRpcRequest);
                    break;
                case PROTOBUF:
                    PbRpcRequest pbRpcRequest = convertPbRpcRequest(rpcRequest);
                    out.add(pbRpcRequest);
                    break;
                default:
            }
        }

        // 响应编码
        if (msg instanceof RpcResponse) {
            RpcResponse rpcResponse = (RpcResponse) msg;
            switch (serializer) {
                case JSON:
                    JsonRpcResponse jsonRpcResponse = JsonRpcResponse.rpcResponseTo(rpcResponse);
                    out.add(jsonRpcResponse);
                    break;
                case PROTOBUF:
                    PbRpcResponse pbRpcResponse = convertPbRpcResponse(rpcResponse);
                    out.add(pbRpcResponse);
                    break;
                default:
                    logger.error("not found serializer");
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          Object msg,
                          List<Object> out) throws Exception {

        // 请求消息解码
        if (msg instanceof JsonRpcRequest) {
            RpcRequest rpcRequest = JsonRpcRequest.jsonRpcRequestTo((JsonRpcRequest) msg);
            out.add(rpcRequest);
        } else if (msg instanceof PbRpcRequest) {
            PbRpcRequest request = (PbRpcRequest) msg;
            // 请求参数数据，采用 json 进行序列化
            byte[] data = request.getParams().toByteArray();
            Object[] params = mapper.readValue(data, Object[].class);
            RpcRequest rpcRequest = buildRpcRequest(request.getId(), request.getName(), params);
            out.add(rpcRequest);
        }

        // 响应消息解码
        if (msg instanceof JsonRpcResponse) {
            RpcResponse rpcResponse = JsonRpcResponse.jsonRpcResponseTo((JsonRpcResponse) msg);
            out.add(rpcResponse);
        } else if (msg instanceof PbRpcResponse) {
            PbRpcResponse response = (PbRpcResponse) msg;
            byte[] data = response.getResult().toByteArray();
            Object result = mapper.readValue(data, Object.class);
            RpcResponse rpcResponse = buildRpcResponse(response.getId(), response.getCode(), response.getMessage(),
                    result);
            out.add(rpcResponse);
        }
    }

    public RpcRequest buildRpcRequest(String id, String name, Object[] params) {
        return new DefaultRpcRequest(id, name, params);
    }

    public RpcResponse buildRpcResponse(String id, int code, String message, Object result) {
        return new DefaultRpcResponse(id, code, message, result);
    }

    public PbRpcRequest convertPbRpcRequest(RpcRequest request) throws JsonProcessingException {
        byte[] data = mapper.writeValueAsBytes(request.getParams());
        return PbRpcRequest.newBuilder()
                .setId(request.getId())
                .setName(request.getName())
                .setParams(ByteString.copyFrom(data))
                .build();
    }


    /**
     * 构建 PbRpcResponse 时候不允许含有 null 值
     * 否则无法构建，导致客户端收不到消息
     */
    public PbRpcResponse convertPbRpcResponse(RpcResponse response) throws JsonProcessingException {
        // 响应结果 json 序列化数据
        byte[] data = mapper.writeValueAsBytes(response.getResult());
        return PbRpcResponse.newBuilder()
                .setId(response.getId())
                .setCode(response.getCode())
                .setMessage(response.getMessage())
                .setResult(ByteString.copyFrom(data))
                .build();
    }

}
