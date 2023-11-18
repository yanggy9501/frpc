package com.freeing.loadbalancer.context;

import com.freeing.rpc.protocol.meta.ServiceMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接数上下文
 *
 * @author yanggy
 */
public class ConnectionsContext {

    private static final Map<String, Integer> CONNECTION_MAP = new ConcurrentHashMap<>();

    public static void add(ServiceMeta serviceMeta) {
        String key = generateKey(serviceMeta);
        Integer count = CONNECTION_MAP.get(key);
        if (count == null) {
            count = 0;
        }
        count++;
        CONNECTION_MAP.put(key, count);
    }

    public static Integer getValue(ServiceMeta serviceMeta){
        String key = generateKey(serviceMeta);
        return CONNECTION_MAP.get(key);
    }

    private static String generateKey(ServiceMeta serviceMeta) {
        return serviceMeta.getServiceAddr().concat(":").concat(String.valueOf(serviceMeta.getServicePort()));
    }

}
