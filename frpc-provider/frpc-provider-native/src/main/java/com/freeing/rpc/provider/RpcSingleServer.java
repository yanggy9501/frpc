package com.freeing.rpc.provider;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.provider.common.scanner.RpcServiceScanner;
import com.freeing.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单机服务提供者实现
 *
 * @author yanggy
 */
public class RpcSingleServer extends BaseServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    /**
     * 构造单机服务提供者，并完成服务的扫描和注册
     */
    public RpcSingleServer(String serverAddress,  String registryAddress, String registryType,
            String registryLoadBalanceType, String scanPackage, String reflectType,
            int heartbeatInterval, int scanNotActiveChannelInterval, boolean enableResultCache, int resultCacheExpire) {
        // 调用父类构造方法
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType,
            heartbeatInterval, scanNotActiveChannelInterval, enableResultCache, resultCacheExpire);
        try {
            this.handlerMap = RpcServiceScanner.doScanWithRpcServiceAnnotationFilterAndRegistryService(scanPackage, registryService, this.host, this.port);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
        logger.info("RpcSingleServer|服务提供者注册表|{}", JSON.toJSONString(this.handlerMap));
    }
}
