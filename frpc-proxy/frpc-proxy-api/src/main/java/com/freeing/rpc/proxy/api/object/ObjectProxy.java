package com.freeing.rpc.proxy.api.object;

import com.alibaba.fastjson.JSON;
import com.freeing.fpc.chace.result.CacheResultKey;
import com.freeing.fpc.chace.result.CacheResultManager;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.async.IAsyncObjectProxy;
import com.freeing.rpc.proxy.api.consumer.Consumer;
import com.freeing.rpc.proxy.api.future.RPCFuture;
import com.freeing.rpc.ratelimiter.api.RateLimiterInvoker;
import com.freeing.rpc.reflect.api.ReflectInvoker;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 对象代理类
 *
 * @author yanggy
 */
public class ObjectProxy<T> implements InvocationHandler, IAsyncObjectProxy {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);

    /**
     * 接口的Class对象
     */
    private Class<T> clazz;

    /**
     * 服务版本
     */
    private String serviceVersion;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 超时时间
     */
    private long timeout = 15000;

    /**
     * 注册服务
     */
    private RegistryService registryService;

    /**
     * 服务消费者
     */
    private Consumer consumer;

    /**
     * 序列化类型
     */
    private String serializationType;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    private boolean enableResultCache;

    /**
     * 反射调用方法
     */
    private ReflectInvoker reflectInvoker;

    /**
     * 容错Class类
     */
    private Class<?> fallbackClass;

    private CacheResultManager<Object> cacheResultManager;

    /**
     * 限流规则SPI接口
     */
    private RateLimiterInvoker rateLimiterInvoker;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, String serializationType, long timeout,
        RegistryService registryService, Consumer consumer, boolean async
        , boolean oneway, boolean enableResultCache,
        int resultCacheExpire, String reflectType, String fallbackClassName, Class<?> fallbackClass,
        String rateLimiterType, int permits, int milliSeconds) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0){
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.fallbackClass = this.getFallbackClass(fallbackClassName, fallbackClass);
        this.enableRateLimiter = enableRateLimiter;
        this.initRateLimiter(rateLimiterType, permits, milliSeconds);
    }

    private void initRateLimiter(String rateLimiterType, int permits, int milliSeconds) {
        if (enableRateLimiter){
            rateLimiterType = StringUtils.isEmpty(rateLimiterType) ? RpcConstants.DEFAULT_RATELIMITER_INVOKER : rateLimiterType;
            this.rateLimiterInvoker = ExtensionLoader.getExtension(RateLimiterInvoker.class, rateLimiterType);
            this.rateLimiterInvoker.init(permits, milliSeconds);
        }
    }


    private Class<?> getFallbackClass(String fallbackClassName, Class<?> fallbackClass) {
        if (this.isFallbackClassEmpty(fallbackClass)) {
            if (StringUtils.isEmpty(fallbackClassName)) {
                try {
                    fallbackClass = Class.forName(fallbackClassName);
                } catch (ClassNotFoundException e) {
                    logger.error("Fail to load class", e);                }
            }
        }
        return fallbackClass;
    }

    private boolean isFallbackClassEmpty(Class<?> fallbackClass) {
        return fallbackClass == null
            || fallbackClass == void.class;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            // 调用 equals 方法
            if ("equals".equals(methodName)) {
                return proxy == args[0];
            }
            // 调用 hashcode 方法
            else if ("hashcode".equals(methodName)) {
                return proxy.getClass().getName()
                    + "@" + Integer.toHexString(System.identityHashCode(proxy))
                    + ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        if (enableResultCache) {
            return invokeSendRequestMethodCache(method, args);
        } else {
            return invokeSendRequestMethodWithRateLimiter(method, args);
        }
   }

    private Object invokeSendRequestMethodCache(Method method, Object[] args) throws Exception {
        CacheResultKey key = new CacheResultKey(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, serviceVersion, serviceGroup);
        Object obj = this.cacheResultManager.get(key);
        if (Objects.isNull(obj)) {
            obj = invokeSendRequestMethodWithRateLimiter(method, args);
            if (Objects.nonNull(obj)) {
                key.setCacheTimeStamp(System.currentTimeMillis());
                this.cacheResultManager.put(key, obj);
            }
        }
        return obj;
    }

    private Object invokeSendRequestMethod(Method method, Object[] args) throws Exception {
        try {
            RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
            requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(this.serializationType, RpcType.REQUEST.getType()));

            RpcRequest request = new RpcRequest();
            request.setVersion(this.serviceVersion);
            request.setClassName(method.getDeclaringClass().getName());
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setGroup(this.serviceGroup);
            request.setParameters(args);
            request.setAsync(async);
            request.setOneway(oneway);
            requestRpcProtocol.setBody(request);

            logger.debug("ObjectProxy#invoke|消费者代码请求头请求体准备完毕|{}", JSON.toJSONString(requestRpcProtocol));

            RPCFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol, registryService);

            return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
        } catch (Throwable t) {
            if (this.isFallbackClassEmpty(fallbackClass)) {
                return null;
            }
            return getFallbackResult(method, args);
        }

    }

    private Object invokeSendRequestMethodWithRateLimiter(Method method, Object[] args) throws Exception {
        Object result = null;
        if (enableRateLimiter) {
            if (rateLimiterInvoker.tryAcquire()) {
                try {
                    result = invokeSendRequestMethod(method, args);
                }finally {
                    rateLimiterInvoker.release();
                }
            } else {
                //TODO 执行各种策略
            }
        } else {
            result = invokeSendRequestMethod(method, args);
        }
        return result;
    }

    private Object getFallbackResult(Method method, Object[] args) {
        try {
            return reflectInvoker.invokeMethod(fallbackClass.newInstance(), fallbackClass, method.getName(), method.getParameterTypes(), args);
        } catch (Throwable ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);
        RPCFuture rpcFuture = null;
        try {
            rpcFuture = this.consumer.sendRequest(request, registryService);
        } catch (Exception e) {
            logger.error("async all throws exception", e);
        }
        return rpcFuture;
    }

    private RpcProtocol<RpcRequest> createRequest(String className, String methodName, Object[] args) {
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();

        RpcHeader header = RpcHeaderFactory.getRequestHeader(this.serializationType, RpcType.REQUEST.getType());
        requestRpcProtocol.setHeader(header);

        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(this.serviceVersion);
        request.setGroup(this.serviceGroup);

        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
        requestRpcProtocol.setBody(request);

        return requestRpcProtocol;
    }

    private Class<?> getClassType(Object obj){
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName){
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return classType;
    }
}
