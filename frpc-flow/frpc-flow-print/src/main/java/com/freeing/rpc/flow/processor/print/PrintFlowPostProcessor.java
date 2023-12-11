package com.freeing.rpc.flow.processor.print;

import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打印处理
 * @author yanggy
 */
@SPIClass
public class PrintFlowPostProcessor implements FlowPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PrintFlowPostProcessor.class);

    @Override
    public void postRpcHeaderProcessor(RpcHeader rpcHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append("magic: " + rpcHeader.getMagic());
        sb.append(", requestId: " + rpcHeader.getRequestId());
        sb.append(", msgType: " + rpcHeader.getMsgType());
        sb.append(", serializationType: " + rpcHeader.getSerializationType());
        sb.append(", status: " + rpcHeader.getStatus());
        sb.append(", msgLen: " + rpcHeader.getMsgLen());
        logger.info(sb.toString());
    }
}
