package com.freeing.rpc.common.scanner.reference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yanggy
 */
public class RpcReferenceContext {
    private static volatile Map<String, Object> INSTANCE = new ConcurrentHashMap<>();

    public static void put(String key, Object value) {
        INSTANCE.put(key, value);
    }

    public static Object get(String key) {
        return INSTANCE.get(key);
    }

    public static Object remove(String key){
        return INSTANCE.remove(key);
    }
}
