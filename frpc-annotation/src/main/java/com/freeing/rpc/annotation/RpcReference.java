package com.freeing.rpc.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc服务消费者
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface RpcReference {
    /**
     * 版本号
     */
    String version() default "1.0.0";

    /**
     *  注册中心类型
     */
    String registryType() default "zookeeper";

    /**
     * 注册中心地址
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * 负载均衡策略，默认基于ZK的一致性Hash
     */
    String loadBalanceType() default "random";

    /**
     * 序列化类型，目前的类型包含：protostuff、kryo、json、jdk、hessian2、fst
     */
    String serializationType() default "protostuff";

    /**
     * 超时时间，默认5s
     */
    long timeout() default 5000;

    /**
     * 是否异步执行
     */
    boolean async() default false;

    /**
     * 是否单向调用
     */
    boolean oneway() default false;

    /**
     * 代理的类型，jdk：jdk代理， javassist: javassist代理, cglib: cglib代理
     */
    String proxy() default "jdk";

    /**
     * 服务分组，默认为空
     */
    String group() default "";

    /**
     * 心跳间隔时间，默认30秒
     */
    int heartbeatInterval() default 30000;

    /**
     * 扫描空闲连接间隔时间，默认60秒
     */
    int scanNotActiveChannelInterval() default 60000;

    /**
     * 重试间隔时间
     */
    int retryInterval() default 1000;

    /**
     * 重试间隔时间
     */
    int retryTimes() default 3;

    /**
     * 容错class
     */
    Class<?> fallbackClass() default void.class;

    /**
     * 容错class名称
     */
    String fallbackClassName() default "";

    /**
     * 反射类型
     */
    String reflectType() default "jdk";

    /**
     * 限流类型
     */
    String rateLimiterType() default "counter";

    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    int permits() default 100;;

    /**
     * 毫秒数
     */
    int milliSeconds() default 1000;
}
