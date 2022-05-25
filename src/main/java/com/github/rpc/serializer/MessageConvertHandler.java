package com.github.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.*;
import com.github.rpc.serializer.json.JsonRpcRequest;
import com.github.rpc.serializer.json.JsonRpcResponse;
import com.github.rpc.serializer.protobuf.PbConverter;
import com.github.rpc.serializer.protobuf.PbRpcRequest;
import com.github.rpc.serializer.protobuf.PbRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
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
                    PbRpcRequest pbRpcRequest = PbConverter.rpcToPbRequest(rpcRequest);
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
                    PbRpcResponse pbRpcResponse = PbConverter.rpcToPbResponse(rpcResponse);
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
            RpcRequest rpcRequest = PbConverter.pbRequestToRpc((PbRpcRequest) msg);
            out.add(rpcRequest);
        }

        // 响应消息解码
        if (msg instanceof JsonRpcResponse) {
            RpcResponse rpcResponse = JsonRpcResponse.jsonRpcResponseTo((JsonRpcResponse) msg);
            out.add(rpcResponse);
        } else if (msg instanceof PbRpcResponse) {
            RpcResponse rpcResponse = PbConverter.pbResponseToRpc((PbRpcResponse) msg);
            out.add(rpcResponse);
        }
    }

}
