package com.freeing.rpc.spi.factory;

import com.freeing.rpc.spi.annotation.SPI;
import com.freeing.rpc.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * 扩展类加载工厂接口
 */
public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
        return Optional.ofNullable(clazz)
            .filter(Class::isInterface)
            .filter(cls -> cls.isAnnotationPresent(SPI.class))
            .map(ExtensionLoader::getExtensionLoader)
            .map(ExtensionLoader::getDefaultSpiClassInstance)
            .orElse(null);
    }
}
