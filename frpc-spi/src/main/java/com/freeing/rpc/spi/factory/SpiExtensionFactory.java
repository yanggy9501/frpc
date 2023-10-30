package com.freeing.rpc.spi.factory;

/**
 * 扩展类加载工厂接口
 */
public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
        return null;
    }
}
