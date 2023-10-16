package com.freeing.rpc.protocol.header;

import com.freeing.rpc.common.id.IdFactory;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.enumeration.RpcType;

/**
 * @author yanggy
 */
public class RpcHeaderFactory {
    public static RpcHeader getRequestHeader(String serializationType) {
        RpcHeader rpcHeader = new RpcHeader();
        long requestId = IdFactory.getId();
        rpcHeader.setMagic(RpcConstants.MAGIC);
        rpcHeader.setRequestId(requestId);
        rpcHeader.setMsgType((byte) RpcType.REQUEST.getType());
        rpcHeader.setStatus((byte) 0x1);
        rpcHeader.setSerializationType(serializationType);
        return rpcHeader;
    }
}
