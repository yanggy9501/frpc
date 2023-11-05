package com.freeing.rpc.refelct.jdk;

import com.freeing.rpc.reflect.api.ReflectInvoker;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * JDK反射调用方法的类
 *
 * @author yanggy
 */
@SPIClass
public class JdkReflectInvoker implements ReflectInvoker {
    private static final Logger logger = LoggerFactory.getLogger(JdkReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes,
            Object[] parameters) throws Throwable {
        logger.info("use jdk reflect type invoke method ...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
