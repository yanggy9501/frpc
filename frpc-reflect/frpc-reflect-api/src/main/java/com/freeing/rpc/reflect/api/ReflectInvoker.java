package com.freeing.rpc.reflect.api;

import com.freeing.rpc.spi.annotation.SPI;

/**
 * 反射方法的调用接口
 */
@SPI
public interface ReflectInvoker {

    /**
     * 调用真实方法的SPI通用接口
     *
     * @param serviceBean 方法所在的对象实例
     * @param serviceClass 方法所在对象实例的Class对象
     * @param methodName 方法的名称
     * @param parameterTypes 方法的参数类型数组
     * @param parameters 方法的参数数组
     * @return 方法调用的结果信息
     * @throws Throwable
     */
    Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes,
        Object[] parameters) throws Throwable;
}
