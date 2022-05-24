package com.github.rpc.serializer.protobuf;

import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 *
 *
 * @author Ray
 * @date created in 2022/5/23 20:22
 */
public class PbRpcRequestDecoder extends ProtobufDecoder {

    public PbRpcRequestDecoder() {
        super(PbRpcRequest.getDefaultInstance());
    }

}
