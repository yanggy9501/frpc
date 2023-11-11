package com.freeing.rpc.registry.api;

import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.spi.annotation.SPI;

import java.io.IOException;

@SPI
public interface RegistryService {

    /** 服务注册
     *
     * @param serviceMeta 服务元数据
     * @throws Exception 抛出异常
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务取消注册
     *
     * @param serviceMeta 服务元数据
     * @throws Exception 抛出异常
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceName 服务名称
     * @param invokerHashCode HashCode值
     * @param sourceIp 源IP地址
     * @return 服务元数据
     * @throws Exception 抛出异常
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception;

    /**
     * 服务销毁
     *
     * @throws IOException 抛出异常
     */
    void destroy() throws IOException;

    /**
     * 默认初始化方法
     */
    default void init(RegistryConfig registryConfig) throws Exception {

    }
}
