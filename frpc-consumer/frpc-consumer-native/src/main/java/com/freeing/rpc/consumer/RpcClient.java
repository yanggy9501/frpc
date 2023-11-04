package com.freeing.rpc.consumer;

import com.freeing.rpc.common.exception.RegistryException;
import com.freeing.rpc.consumer.common.RpcConsumer;
import com.freeing.rpc.proxy.api.ProxyFactory;
import com.freeing.rpc.proxy.api.async.IAsyncObjectProxy;
import com.freeing.rpc.proxy.api.config.ProxyConfig;
import com.freeing.rpc.proxy.api.object.ObjectProxy;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.registry.zookeeper.ZookeeperRegistryService;
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

    public RpcClient(String registryAddress, String registryType,  String proxy, String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.proxy = proxy;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress, registryType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registry type cant not be empty");
        }
        //TODO 后续SPI扩展
        RegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }


    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig<>(
            interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, registryService,
            RpcConsumer.getInstance(), async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, registryService,
            RpcConsumer.getInstance(), async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
