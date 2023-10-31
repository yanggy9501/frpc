package com.freeing.rpc.serialization.api;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.spi.annotation.SPI;

/**
 * 序列化接口
 */
@SPI(RpcConstants.SERIALIZATION_JDK)
public interface Serialization {
    /**
     * 序列化
     *
     * @param object
     * @return
     */
    <T> byte[] serialize(T object);

    /**
     * 反序列化
     *
     * @param bytes 序列化字节数组
     * @param clazz 反序列化对象类型
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
