package com.freeing.rpc.provider.common.scanner;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.scanner.ClassScanner;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @RpcService注解扫描器
 *
 * @author yanggy
 */
public class RpcServiceScanner extends ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解标注的类
     */
    public static Map<String, Object> doScanWithRpcServiceAnnotationFilterAndRegistryService(String scanPackage,
            RegistryService registryService, String host, int port)
        throws IOException, ClassNotFoundException {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (Objects.isNull(classNameList) || classNameList.isEmpty()) {
            return handlerMap;
        }
        for (String className : classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    // 优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcService), rpcService.version(), rpcService.group(), host, port);
                    // 将元数据注册到注册中心
                    registryService.register(serviceMeta);
                    String key = RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup());
                    handlerMap.put(key, clazz.newInstance());
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception", e);
            }
        }
        return handlerMap;
    }


    private static String getServiceName(RpcService rpcService) {
        Class<?> interfaceClass = rpcService.interfaceClass();
        if (interfaceClass == void.class) {
            return rpcService.interfaceClassName();
        }
        String serviceName = interfaceClass.getName();
        if (Objects.isNull(serviceName)  || serviceName.trim().isEmpty()) {
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}
