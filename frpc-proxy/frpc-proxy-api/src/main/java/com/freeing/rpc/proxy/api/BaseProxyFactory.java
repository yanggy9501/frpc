package com.freeing.rpc.proxy.api;

import com.freeing.rpc.proxy.api.config.ProxyConfig;
import com.freeing.rpc.proxy.api.object.ObjectProxy;

/**
 * 基础代理工厂类
 *
 * @author yanggy
 */
public abstract class BaseProxyFactory<T> implements ProxyFactory {
    /**
     * 接口的代理对象
     */
    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(proxyConfig.getClazz(),
            proxyConfig.getServiceVersion(),
            proxyConfig.getServiceGroup(),
            proxyConfig.getSerializationType(),
            proxyConfig.getTimeout(),
            proxyConfig.getRegistryService(),
            proxyConfig.getConsumer(),
            proxyConfig.getAsync(),
            proxyConfig.getOneway());
    }
}
