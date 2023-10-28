package com.freeing.rpc.proxy.jdk;

import com.freeing.rpc.proxy.api.BaseProxyFactory;
import com.freeing.rpc.proxy.api.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * JDK动态代理
 *
 * @author yanggy
 */
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    // TODO 有重复的信息可以优化
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class<?>[]{clazz},
            objectProxy
        );
    }
}
