package com.freeing.rpc.consumer.spring;

import com.freeing.rpc.consumer.RpcClient;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author yanggy
 */
public class RpcReferenceBean implements FactoryBean<Object> {
    /**
     * 接口类型
     */
    private Class<?> interfaceClass;

    /**
     * 版本号
     */
    private String version;

    /**
     * 注册中心类型：zookeeper/nacos/apoll/etcd/eureka等
     */
    private String registryType;

    /**
     * 负载均衡类型：zkconsistenthash
     */
    private String loadBalanceType;

    /**
     * 序列化类型：fst/kryo/protostuff/jdk/hessian2/json
     */
    private String serializationType;

    /**
     * 注册中心地址
     */
    private String registryAddress;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 服务分组
     */
    private String group;

    /**
     * 是否异步
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    /**
     * 代理方式
     */
    private String proxy;

    /**
     * 生成的代理对象
     */
    private Object object;

    /**
     * 扫描空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval;

    /**
     * 心跳检测时间
     */
    private int heartbeatInterval;

    // 重试间隔时间
    private int retryInterval = 1000;

    // 重试次数
    private int retryTimes = 3;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    private int resultCacheExpire;

    private RpcClient rpcClient;

    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 容错类Class名称
     */
    private String fallbackClassName;

    /**
     * 容错类
     */
    private Class<?> fallbackClass;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getLoadBalanceType() {
        return loadBalanceType;
    }

    public void setLoadBalanceType(String loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getScanNotActiveChannelInterval() {
        return scanNotActiveChannelInterval;
    }

    public void setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public boolean getEnableResultCache() {
        return enableResultCache;
    }

    public void setEnableResultCache(boolean enableResultCache) {
        this.enableResultCache = enableResultCache;
    }

    public int getResultCacheExpire() {
        return resultCacheExpire;
    }

    public void setResultCacheExpire(int resultCacheExpire) {
        this.resultCacheExpire = resultCacheExpire;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public String getReflectType() {
        return reflectType;
    }

    public void setReflectType(String reflectType) {
        this.reflectType = reflectType;
    }

    public String getFallbackClassName() {
        return fallbackClassName;
    }

    public void setFallbackClassName(String fallbackClassName) {
        this.fallbackClassName = fallbackClassName;
    }

    public Class<?> getFallbackClass() {
        return fallbackClass;
    }

    public void setFallbackClass(Class<?> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        RpcClient rpcClient = new RpcClient(registryAddress, registryType, loadBalanceType, proxy, version, group,
            serializationType, timeout, async, oneway, heartbeatInterval, scanNotActiveChannelInterval, retryInterval,
            retryTimes, enableResultCache, resultCacheExpire, reflectType, fallbackClassName);
        rpcClient.setFallbackClass(fallbackClass);
        this.object = rpcClient.create(interfaceClass);
    }
}
