package com.freeing.rpc.proxy.jdk;

import com.freeing.rpc.proxy.api.BaseProxyFactory;

import java.lang.reflect.Proxy;

/**
 * JDK动态代理
 *
 * @author yanggy
 */
public class JdkProxyFactory<T> extends BaseProxyFactory<T> {

    // TODO 有重复的信息可以优化
    @Override
    public <T> T getProxy(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class<?>[]{clazz},
            objectProxy
        );
    }
}
