package com.freeing.rpc.spi.factory;

import com.freeing.rpc.spi.annotation.SPI;

/**
 * 扩展类加载工厂接口
 */
@SPI("spi")
public interface ExtensionFactory {
    /**
     * 获取扩展类对象
     *
     * @param <T>  泛型类型
     * @param key  传入的key值
     * @param clazz Class类型对象
     * @return 扩展类对象
     */
    <T> T getExtension(String key, Class<T> clazz);
}
