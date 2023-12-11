package com.freeing.rpc.flow.processor;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.spi.annotation.SPI;

/**
 * @author yanggy
 */
@SPI(RpcConstants.FLOW_POST_PROCESSOR_PRINT)
public interface FlowPostProcessor {

    /**
     * 流控分析后置处理器方法
     *
     * @param header
     */
    void postRpcHeaderProcessor(RpcHeader header);
}
