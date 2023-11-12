package com.freeing.rpc.provider.common.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.threadpool.ServerThreadPool;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcStatus;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * RPC服务提供者的Handler处理类
 *
 * @author yanggy
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private static final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    private String reflectType;
    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(String reflectType, Map<String, Object> handlerMap){
        this.reflectType = reflectType;
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        ServerThreadPool.submit(() -> {
            RpcHeader header = protocol.getHeader();
            RpcProtocol<RpcResponse>  responseRpcProtocol = handlerMessage(protocol);
            ctx.writeAndFlush(responseRpcProtocol)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.debug("Send response for request {}", header.getRequestId());
                    }
                });
        });
    }

    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();
        // 心跳消息
        if (header.getMsgType() == RpcType.HEARTBEAT_FROM_CONSUMER.getType()) {
            responseRpcProtocol = handlerHeartbeatMessage(protocol, header);
        }
        // 请求消息
        else if (header.getMsgType() == RpcType.REQUEST.getType()) {
            responseRpcProtocol = handlerRequestMessage(protocol, header);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcRequest request = protocol.getBody();
        logger.debug("Receive request " + header.getRequestId());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse response = new RpcResponse();
        try {
            Object result = handle(request);
            response.setResult(result);
            response.setAsync(request.getAsync());
            response.setOneway(request.getOneway());
            header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        } catch (Throwable t) {
            response.setError(t.toString());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            logger.error("RPC Server handle request error",t);
        }
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerHeartbeatMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_CONSUMER.getType());
        RpcRequest request = protocol.getBody();
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(RpcConstants.HEARTBEAT_PONG);
        rpcResponse.setAsync(request.getAsync());
        rpcResponse.setOneway(request.getOneway());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(rpcResponse);
        return responseRpcProtocol;
    }

    private Object handle(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String key = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object serviceBean = handlerMap.get(key);
        if (Objects.isNull(serviceBean)) {
            throw new RuntimeException(String.format("service bean instance not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.info("RpcInvoeMehtod|rpc协议解析完成|{}", JSON.toJSONString(request));

        return invoeMehtod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
    }

    private Object invoeMehtod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes,
            Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        switch(this.reflectType) {
            case RpcConstants.REFLECT_TYPE_JDK:
                return invoeJDKMehtod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            case RpcConstants.REFLECT_TYPE_CGLIB:
                return invoeCGLibMehtod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            default:
                throw new IllegalArgumentException("not support reflect type");
        }
    }

    private Object invoeJDKMehtod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes,
        Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logger.info("use jdk reflect type invoke method...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    private Object invoeCGLibMehtod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes,
        Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // cglib reflect
        logger.info("use cglib reflect type invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
