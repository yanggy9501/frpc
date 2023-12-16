package com.freeing.rpc.provider.common.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.fpc.chace.result.CacheResultKey;
import com.freeing.fpc.chace.result.CacheResultManager;
import com.freeing.rpc.buffer.cache.BufferCacheManager;
import com.freeing.rpc.buffer.object.BufferObject;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.threadpool.ServerThreadPool;
import com.freeing.rpc.connection.manager.ConnectionManager;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcStatus;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import com.freeing.rpc.provider.common.cache.ProviderChannelCache;
import com.freeing.rpc.ratelimiter.api.RateLimiterInvoker;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import com.freeing.rpc.threadpool.BufferCacheThreadPool;
import com.freeing.rpc.threadpool.ConcurrentThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateHandler;
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

    /**
     * 是否启用结果缓存
     */
    private final boolean enableResultCache;

    /**
     * 结果缓存管理器
     */
    private final CacheResultManager<RpcProtocol<RpcResponse>> cacheResultManager;


    /**
     * 线程池
     */
    private final ConcurrentThreadPool concurrentThreadPool = ConcurrentThreadPool.getInstance(8, 8);

    /**
     * 连接管理器
     */
    private ConnectionManager connectionManager;

    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区管理器
     */
    private BufferCacheManager<BufferObject<RpcRequest>> bufferCacheManager;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;

    /**
     * 限流SPI接口
     */
    private RateLimiterInvoker rateLimiterInvoker;


    public RpcProviderHandler(String reflectType, Map<String, Object> handlerMap,
        boolean enableResultCache, int resultCacheExpire, int maxConnections, String disuseStrategyType,
        boolean enableBuffer, int bufferSize, String rateLimiterType, int permits, int milliSeconds){
        this.reflectType = reflectType;
        this.handlerMap = handlerMap;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0) {
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.connectionManager = ConnectionManager.getInstance(maxConnections, disuseStrategyType);

        // 开启缓冲
        if (enableBuffer) {
            logger.info("enable buffer...");
            bufferCacheManager = BufferCacheManager.getInstance(bufferSize);
            BufferCacheThreadPool.submit(this::consumerBufferCache);
        }

        this.enableRateLimiter = enableRateLimiter;
        this.initRateLimiter(rateLimiterType, permits, milliSeconds);
    }

    private void initRateLimiter(String rateLimiterType, int permits, int milliSeconds) {
        if (enableRateLimiter) {
            rateLimiterType = rateLimiterType != null && !rateLimiterType.isEmpty() ? rateLimiterType : "counter";
            this.rateLimiterInvoker = ExtensionLoader.getExtension(RateLimiterInvoker.class, rateLimiterType);
            this.rateLimiterInvoker.init(permits, milliSeconds);
        }
    }

    /**
     * 消费缓冲区的数据
     */
    private void consumerBufferCache() {
        while (true) {
            BufferObject<RpcRequest> bufferObject = this.bufferCacheManager.take();
            if (Objects.nonNull(bufferObject)) {
                ChannelHandlerContext ctx = bufferObject.getCtx();
                RpcProtocol<RpcRequest> protocol = bufferObject.getProtocol();
                RpcHeader header = protocol.getHeader();
                RpcProtocol<RpcResponse> responseRpcProtocol = handlerRequestMessageWithCache(protocol, header);
                this.writeAndFlush(header.getRequestId(), ctx, responseRpcProtocol);
            }
        }
    }

    /**
     * 缓冲数据
     */
    private void bufferRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        // 接收到服务消费者发送的心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()){
            RpcProtocol<RpcResponse> responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
            this.writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
        }
        else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()){  //接收到服务消费者响应的心跳消息
            handlerHeartbeatMessageToProvider(protocol, ctx.channel());
        }
        else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()){ //请求消息
            this.bufferCacheManager.put(new BufferObject<>(ctx, protocol));
        }
    }

    private void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
        writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
    }

    private void writeAndFlush(long requestId, ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseRpcProtocol) {
        ctx.writeAndFlush(responseRpcProtocol)
            .addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    logger.debug("Send response for request " + requestId);
                }
            });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
        connectionManager.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果是IdleStateEvent事件
        if (evt instanceof IdleStateHandler) {
            Channel channel = ctx.channel();
            try {
                logger.info("IdleStateEvent triggered, close channel " + channel.remoteAddress());
                connectionManager.remove(channel);
                channel.close();
            } finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        ServerThreadPool.submit(() -> {
            connectionManager.update(ctx.channel());
            // 开启队列缓冲
            if (enableBuffer){
                this.bufferRequest(ctx, protocol);
            }
            // 未开启队列缓冲
            else {
                this.submitRequest(ctx, protocol);
            }
        });
    }

    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol, Channel channel) {
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();
        // 接收到服务消费者发送的心跳消息
        if (header.getMsgType() == RpcType.HEARTBEAT_FROM_CONSUMER.getType()) {
            responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
        }
        //
        else if(header.getMsgType() == RpcType.HEARTBEAT_TO_PROVIDER.getType()) {
            handlerHeartbeatMessageToProvider(protocol, channel);
        }
        // 请求消息
        else if (header.getMsgType() == RpcType.REQUEST.getType()) {
            responseRpcProtocol = handlerRequestMessageWithCacheAndRateLimiter(protocol, header);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessageWithCacheAndRateLimiter(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        if (enableRateLimiter) {
            if (rateLimiterInvoker.tryAcquire()) {
                try {
                    responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
                } finally {
                    rateLimiterInvoker.release();
                }
            } else {
                // TODO 限流策略
            }
        } else {
            responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
        }
        return responseRpcProtocol;
    }

        private RpcProtocol<RpcResponse> handlerRequestMessageWithCache(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        if (enableResultCache) {
            return handlerRequestMessageCache(protocol, header);
        } else {
            return handlerRequestMessage(protocol, header);
        }
    }

    /**
     * 处理缓存
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageCache(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        CacheResultKey key = new CacheResultKey(request.getClassName(), request.getMethodName(), request.getParameterTypes(), request.getParameters(), request.getVersion(), request.getGroup());
        RpcProtocol<RpcResponse> responseRpcProtocol = cacheResultManager.get(key);
        if (Objects.isNull(responseRpcProtocol)) {
            responseRpcProtocol = handlerRequestMessage(protocol, header);
            // 缓存结果
            key.setCacheTimeStamp(System.currentTimeMillis());
            cacheResultManager.put(key, responseRpcProtocol);
        } else {
            logger.info("RpcProvider|命中缓存|{}", header.getRequestId());
        }
        responseRpcProtocol.setHeader(header);
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        logger.info("received consumer request message: {}", JSON.toJSONString(protocol));
        RpcRequest request = protocol.getBody();
        logger.debug("Receive request " + header.getRequestId());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
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

    private RpcProtocol<RpcResponse> handlerHeartbeatMessageFromConsumer(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        logger.info("received consumer heartbeat message: {}", JSON.toJSONString(protocol));
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

    /**
     * 处理服务消费者响应的心跳消息
     */
    private void handlerHeartbeatMessageToProvider(RpcProtocol<RpcRequest> protocol, Channel channel) {
        logger.info("receive service consumer heartbeat message, the consumer is: {}, the heartbeat message is: {}", channel.remoteAddress(), protocol.getBody().getParameters()[0]);
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
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
        ctx.close();
    }
}
