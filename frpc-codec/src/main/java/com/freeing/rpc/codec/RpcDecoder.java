package com.freeing.rpc.codec;

import com.freeing.rpc.common.utils.SerializationUtils;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import com.freeing.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * @author yanggy
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {

    private FlowPostProcessor postProcessor;

    public RpcDecoder(FlowPostProcessor postProcessor){
        this.postProcessor = postProcessor;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();
        short magic = in.readShort();
        if (magic != RpcConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }

        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        // 序列化类型
        ByteBuf serializationTypeByteBuf = in.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_COUNT);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));

        int dataLength = in.readInt();
        // 不是完整的数据
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        RpcType rpcType = RpcType.findByType(msgType);
        if (rpcType == null) {
            return;
        }

        RpcHeader header = new RpcHeader();
        header.setMagic(magic);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setSerializationType(serializationType);
        header.setMsgLen(dataLength);

        Serialization serialization = getSerialization(serializationType);

        switch (rpcType) {
            case REQUEST:
            // 服务消费者发送给服务提供者的心跳数据
            case HEARTBEAT_FROM_CONSUMER:
            // 服务提供者发送给服务消费者的心跳数据
            case HEARTBEAT_TO_PROVIDER: {
                RpcRequest request = serialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            }
            case RESPONSE:
            // 服务提供者响应服务消费者的心跳数据
            case HEARTBEAT_TO_CONSUMER:
            // 服务消费者响应服务提供者的心跳数据
            case HEARTBEAT_FROM_PROVIDER: {
                RpcResponse response = serialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            }
        }

        this.postFlowProcessor(postProcessor, header);
    }
}
