package com.freeing.rpc.codec;

import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import com.freeing.rpc.threadpool.FlowPostProcessorThreadPool;

/**
 * 实现编解码的接口，提供序列化和反序列化的默认方法
 */
public interface RpcCodec {

    /**
     * 据serializationType通过SPI获取序列化句柄
     *
     * @param serializationType
     * @return
     */
    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }

    /**
     * 调用RPC框架流量分析后置处理器
     * @param postProcessor 后置处理器
     * @param header 封装了流量信息的消息头
     */
    default void postFlowProcessor(FlowPostProcessor postProcessor, RpcHeader header){
        FlowPostProcessorThreadPool.submit(() -> postProcessor.postRpcHeaderProcessor(header));
    }
}
