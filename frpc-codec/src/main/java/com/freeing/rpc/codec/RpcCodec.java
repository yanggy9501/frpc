package com.freeing.rpc.codec;

import com.freeing.rpc.serialization.api.Serialization;
import com.freeing.rpc.spi.loader.ExtensionLoader;

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
}
