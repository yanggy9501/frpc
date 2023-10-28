package com.freeing.rpc.proxy.api.object;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.async.IAsyncObjectProxy;
import com.freeing.rpc.proxy.api.consumer.Consumer;
import com.freeing.rpc.proxy.api.future.RPCFuture;
import com.freeing.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, String serializationType, long timeout, RegistryService registryService, Consumer consumer, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
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

        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(this.serializationType));

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

        return rpcFuture == null
            ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
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

        RpcHeader header = RpcHeaderFactory.getRequestHeader(this.serializationType);
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
