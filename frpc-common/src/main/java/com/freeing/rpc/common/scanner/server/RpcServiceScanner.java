package com.freeing.rpc.common.scanner.server;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.scanner.ClassScanner;
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
public class RpcServiceScanner  extends ClassScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    public static Map<String, Object> doScanWithRpcServiceAnnotationFilterAndRegistryService(String scanPackage)
            throws IOException, ClassNotFoundException {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (Objects.isNull(classNameList) || classNameList.isEmpty()) {
            return handlerMap;
        }
        for (String className : classNameList) {
            Class<?> clazz = Class.forName(className);
            RpcService rpcService = clazz.getAnnotation(RpcService.class);
            if (rpcService != null) {
                try {
                    //优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    //TODO 后续逻辑向注册中心注册服务元数据，同时向handlerMap中记录标注了RpcService注解的类实例
                    //handlerMap中的Key先简单存储为serviceName+version+group，后续根据实际情况处理key
                    String serviceName = getServiceName(rpcService);
                    String key = RpcServiceHelper.buildServiceKey(serviceName, rpcService.version(), rpcService.group());
                    handlerMap.put(key, clazz.newInstance());
                } catch (Exception e) {
                    LOGGER.error("scan classes throws exception", e);
                }
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
