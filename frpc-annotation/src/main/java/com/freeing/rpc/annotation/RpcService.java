package com.freeing.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc服务提供者注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    /**
     * rpc服务接口
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 接口的ClassName
     */
    @Deprecated
    String interfaceClassName() default "";


    /**
     *  版本号
     */
    String version() default "1.0.0";

    /**
     * 服务分组
     */
    String group() default "";

    /**
     * 权重
     */
    int weight() default 0;
}
