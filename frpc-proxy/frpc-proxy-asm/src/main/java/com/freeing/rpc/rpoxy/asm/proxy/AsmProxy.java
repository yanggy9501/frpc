package com.freeing.rpc.rpoxy.asm.proxy;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义ASM代理
 *
 * @author yanggy
 */
public class AsmProxy {
    private static final String PROXY_CLASS_NAME_PRE = "$Proxy";
    private static final AtomicInteger PROXY_CNT = new AtomicInteger(0);



    public static Object newProxyInstance(ClassLoader classLoader,
        Class<?> interfaces,
        InvocationHandler invocationHandler) {

        return null;
    }

    private static Class<?> generate(Class<?> interfaces) {
        String proxyClassName = PROXY_CLASS_NAME_PRE + PROXY_CNT.getAndIncrement();
        return null;
    }
}
