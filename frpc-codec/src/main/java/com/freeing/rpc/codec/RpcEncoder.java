package com.freeing.rpc.codec;

import com.freeing.rpc.common.utils.SerializationUtils;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 实现RPC编码
 *
 * @author yanggy
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec {

    private FlowPostProcessor postProcessor;

    public RpcEncoder(FlowPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        RpcHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        String serializationType = header.getSerializationType();
        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes("UTF-8"));
        // TODO  Serialization是扩展点
        Serialization serialization = getSerialization(serializationType);
        byte[] data = serialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

        //异步调用流控分析后置处理器
        header.setMsgLen(data.length);
        this.postFlowProcessor(postProcessor, header);
    }
}
