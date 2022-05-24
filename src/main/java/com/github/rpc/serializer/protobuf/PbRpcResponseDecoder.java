package com.github.rpc.serializer.protobuf;

import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 * @author Ray
 * @date created in 2022/5/23 21:04
 */
public class PbRpcResponseDecoder extends ProtobufDecoder {

    public PbRpcResponseDecoder() {
        super(PbRpcResponse.getDefaultInstance());
    }

}
