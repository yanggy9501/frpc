package com.freeing.rpc.provider.spring;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.provider.common.server.base.BaseServer;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 基于Spring启动RPC服务
 *
 * @author yanggy
 */
public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcSpringServer.class);

    public RpcSpringServer(String serverAddress, String registryAddress, String registryType,
        String registryLoadBalanceType, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval,
        boolean enableResultCache, int resultCacheExpire, String flowType, int maxConnections, String disuseStrategyType,
        boolean enableBuffer, int bufferSize) {
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType,
            heartbeatInterval, scanNotActiveChannelInterval, enableResultCache, resultCacheExpire, flowType,
            maxConnections, disuseStrategyType, enableBuffer, bufferSize);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                ServiceMeta serviceMeta = new ServiceMeta(
                    this.getServiceName(rpcService),
                    rpcService.version(),
                    rpcService.group(),
                    host,
                    port,
                    getWeight(rpcService.weight())
                );
                // 将 spring 创建的 bean 作为最终的 bean；不需要自己去扫描
                handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()),
                    serviceBean);
                try {
                    registryService.register(serviceMeta);
                } catch (Exception e) {
                    logger.error("rpc server init spring exception", e);
                }
            }
        }
    }

    private String getServiceName(RpcService rpcService) {
        // 优先使用interfaceClass
        Class<?> clazz = rpcService.interfaceClass();
        if (clazz == null) {
            return rpcService.interfaceClassName();
        }
        return clazz.getName();
    }

    private int getWeight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN){
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX){
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startNettyServer();
    }
}
