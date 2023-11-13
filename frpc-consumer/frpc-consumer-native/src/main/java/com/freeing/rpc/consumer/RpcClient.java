package com.freeing.rpc.consumer;

import com.freeing.rpc.common.exception.RegistryException;
import com.freeing.rpc.consumer.common.RpcConsumer;
import com.freeing.rpc.proxy.api.ProxyFactory;
import com.freeing.rpc.proxy.api.async.IAsyncObjectProxy;
import com.freeing.rpc.proxy.api.config.ProxyConfig;
import com.freeing.rpc.proxy.api.object.ObjectProxy;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanggy
 */
public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 注册服务
     */
    private RegistryService registryService;

    /**
     * 服务版本
     */
    private String serviceVersion;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 序列化类型
     */
    private String serializationType;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    private String proxy;

    /**
     * 心跳间隔时间，默认30秒
     */
    private int heartbeatInterval;

    /**
     * 扫描空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval;

    public RpcClient(String registryAddress, String registryType, String registryLoadBalanceType, String proxy,
        String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async,
        boolean oneway, int heartbeatInterval, int scanNotActiveChannelInterval) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.proxy = proxy;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registry type cant not be empty");
        }

        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }


    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig<>(
            interfaceClass,
            serviceVersion,
            serviceGroup,
            serializationType,
            timeout,
            registryService,
            RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval),
            async,
            oneway)
        );
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, registryService,
            RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval), async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval).close();
    }
}
