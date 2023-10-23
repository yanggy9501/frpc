package com.freeing.rpc.proxy.api;

import com.freeing.rpc.proxy.api.config.ProxyConfig;

/**
 * 代理工厂接口
 */
public interface ProxyFactory {
    /**
     * 获取代理对象
     */
    <T> T getProxy(Class<?> clazz);

    /**
     * 默认初始化方法
     */
    default <T> void init(ProxyConfig<T> proxyConfig) {

    }
}
