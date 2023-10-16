package com.freeing.rpc.common.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 简易ID工厂类
 *
 * @author yanggy
 */
public class IdFactory {
    private final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    public static Long getId(){
        return REQUEST_ID_GEN.incrementAndGet();
    }
}
