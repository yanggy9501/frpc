package com.freeing.rpc.proxy.jdk;

import com.freeing.rpc.proxy.api.BaseProxyFactory;
import com.freeing.rpc.proxy.api.ProxyFactory;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * JDK动态代理
 *
 * @author yanggy
 */
@SPIClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(JdkProxyFactory.class);

    // TODO 有重复的信息可以优化
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        logger.info("get jdk proxy object. {}", clazz.getCanonicalName());
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class<?>[]{clazz},
            objectProxy
        );
    }
}
