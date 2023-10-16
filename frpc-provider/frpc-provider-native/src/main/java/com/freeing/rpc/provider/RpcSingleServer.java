package com.freeing.rpc.provider;

import com.freeing.rpc.common.scanner.server.RpcServiceScanner;
import com.freeing.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanggy
 */
public class RpcSingleServer extends BaseServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress, String scanPackage) {
        // 调用父类构造方法
        super(serverAddress);
        try {
            this.handlerMap = RpcServiceScanner.doScanWithRpcServiceAnnotationFilterAndRegistryService(scanPackage);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }
}
