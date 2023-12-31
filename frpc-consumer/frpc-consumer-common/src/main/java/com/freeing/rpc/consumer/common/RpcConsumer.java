package com.freeing.rpc.consumer.common;

import com.freeing.loadbalancer.context.ConnectionsContext;
import com.freeing.rpc.common.exception.RpcException;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.threadpool.ClientThreadPool;
import com.freeing.rpc.common.utils.ip.IpUtils;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.consumer.common.handler.RpcConsumerHandler;
import com.freeing.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import com.freeing.rpc.consumer.common.initializer.RpcConsumerInitializer;
import com.freeing.rpc.consumer.common.manager.ConsumerConnectionManager;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.consumer.Consumer;
import com.freeing.rpc.proxy.api.future.RPCFuture;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import com.freeing.rpc.threadpool.ConcurrentThreadPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务消费者 netty client
 *
 * @author yanggy
 */
public class RpcConsumer implements Consumer {
    private static final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    private final String localIp;

    private static volatile RpcConsumer instance;

    private ScheduledExecutorService executorService;

    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    /**
     * 重试间隔时间
     */
    private int retryInterval = 1000;

    /**
     * 重试次数
     */
    private int retryTimes = 3;


    /**
     * 当前重试次数
     */
    private volatile int currentConnectRetryTimes = 0;

    /**
     * 心跳间隔时间，默认30秒
     */
    private int heartbeatInterval = 30000;

    /**
     * 扫描并移除空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval = 60000;

    /**
     * 是否开启直连服务
     */
    private boolean enableDirectServer = false;

    /**
     * 直连服务的地址
     */
    private String directServerUrl;

    // 并发处理线程池
    private ConcurrentThreadPool concurrentThreadPool = ConcurrentThreadPool.getInstance(8, 8);

    // 流控分析后置处理器
    private FlowPostProcessor flowPostProcessor =  ExtensionLoader.getExtension(FlowPostProcessor.class, RpcConstants.FLOW_POST_PROCESSOR_PRINT);

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer = true;

    /**
     * 缓冲区大小
     */
    private int bufferSize = Integer.MAX_VALUE;

    private RpcConsumer(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        this.retryTimes = retryTimes <= 0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;
        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new RpcConsumerInitializer(heartbeatInterval, enableBuffer, bufferSize, concurrentThreadPool, flowPostProcessor));
        this.startHeartbeat();
    }

    private RpcConsumer() {
        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
            .handler(new RpcConsumerInitializer(heartbeatInterval, enableBuffer, bufferSize, concurrentThreadPool, flowPostProcessor));
        this.startHeartbeat();
    }

    public RpcConsumer setEnableDirectServer(boolean enableDirectServer) {
        this.enableDirectServer = enableDirectServer;
        return this;
    }

    public RpcConsumer setDirectServerUrl(String directServerUrl) {
        this.directServerUrl = directServerUrl;
        return this;
    }

    public RpcConsumer setHeartbeatInterval(int heartbeatInterval) {
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        return this;
    }

    public RpcConsumer setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        if (scanNotActiveChannelInterval > 0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        return this;
    }

    public RpcConsumer setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        return this;
    }

    public RpcConsumer setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes <= 0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;
        return this;
    }

    public static RpcConsumer getInstance(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        if (instance == null){
            synchronized (RpcConsumer.class){
                if (instance == null){
                    instance = new RpcConsumer(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes);
                }
            }
        }
        return instance;
    }

    public void close(){
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    @Override
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper
            .buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] params = request.getParameters();
        int invokerHashCode =  (params == null || params.length <= 0) ? serviceKey.hashCode() : params[0].hashCode();
        ServiceMeta serviceMeta = this.getDirectServiceMetaOrWithRetry(registryService, serviceKey, invokerHashCode);

        RpcConsumerHandler handler = null;
        if (Objects.nonNull(serviceMeta)) {
            handler = getRpcConsumerHandlerWithRetry(serviceMeta);
        }
        RPCFuture rpcFuture = null;
        if (handler != null){
            rpcFuture = handler.sendRequest(protocol, request.getAsync(), request.getOneway());
        }
        return rpcFuture;
    }

    private ServiceMeta getDirectServiceMeta() {
        if (StringUtils.isEmpty(directServerUrl)) {
            throw new RpcException("direct server url is null.");
        }
        if (!directServerUrl.contains(RpcConstants.IP_PORT_SPLIT)) {
            throw new RpcException("direct server url not contains ':'");
        }
        logger.info("服务消费者直连服务提供者===>>> {}", directServerUrl);
        ServiceMeta serviceMeta = new ServiceMeta();
        String[] directServerUrlArray = directServerUrl.split(RpcConstants.IP_PORT_SPLIT);
        serviceMeta.setServiceAddr(directServerUrlArray[0]);
        serviceMeta.setServicePort(Integer.parseInt(directServerUrlArray[1]));
        return serviceMeta;
    }

    /**
     * 重试获取服务提供者元数据
     */
    private ServiceMeta getServiceMetaWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        logger.info("获取服务提供者元数据...");
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
        if (Objects.isNull(serviceMeta)) {
            // 启动重试机制
            for (int i = 0; i < retryTimes; i++) {
                logger.info("获取服务提供者元数据第【{}】次重试...", i);
                serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
                if (serviceMeta != null){
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }
        return serviceMeta;
    }

    /**
     * 创建连接并返回 RpcConsumerHandler
     *
     * @param serviceMeta
     * @return
     * @throws InterruptedException
     */
    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta serviceMeta) throws Exception {
        ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
        channelFuture.addListener((ChannelFutureListener) -> {
            if (channelFuture.isSuccess()) {
                logger.info("connect rpc server {} on port {} success.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                // 添加连接信息，在服务消费者端记录每个服务提供者实例的连接次数
                ConnectionsContext.add(serviceMeta);
                // 连接成功，将当前连接重试次数设置为0
                currentConnectRetryTimes = 0;
            } else {
                logger.error("connect rpc server {} on port {} failed.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }

    /**
     * 直连服务提供者或者结合重试获取服务元数据信息
     */
    private ServiceMeta getDirectServiceMetaOrWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        ServiceMeta serviceMeta;
        if (enableDirectServer) {
            serviceMeta = this.getDirectServiceMeta();
        } else {
            serviceMeta = getServiceMetaWithRetry(registryService, serviceKey, invokerHashCode);
        }
        return serviceMeta;
    }

    /**
     * 重试获取服务提供者元数据
     */
    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(ServiceMeta serviceMeta) throws InterruptedException {
        logger.info("服务消费者连接服务提供者...");
        RpcConsumerHandler handler = null;
        try {
            handler = this.getRpcConsumerHandlerWithCache(serviceMeta);
        }
        // 连接异常则重试
        catch (Exception e) {
            logger.info("fail to connect to provider", e);
            if (e instanceof ConnectException) {
                // 启动重试
                if (currentConnectRetryTimes < retryTimes) {
                    logger.info("服务消费者连接服务提供者第[{}]次重试...", currentConnectRetryTimes);
                    handler = getRpcConsumerHandlerWithRetry(serviceMeta);
                    Thread.sleep(retryInterval);
                }
            }
        }
        return handler;
    }

    /**
     * 获取 RpcConsumerHandler
     */
    private RpcConsumerHandler getRpcConsumerHandlerWithCache(ServiceMeta serviceMeta) throws Exception {
        RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
        // 缓存中不存在
        if (Objects.isNull(handler)) {
            // 创建并返回新的连接
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        } else if (!handler.getChannel().isActive()) {
            handler.close();
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }
        return handler;
    }

    /**
     * 广播发送 ping 消息
     */
    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        // 扫描并处理所有不活跃的连接
        executorService.scheduleAtFixedRate(() -> {
//            logger.info("=============scanNotActiveChannel============");
            ConsumerConnectionManager.scanNoTActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.SECONDS);

        executorService.scheduleAtFixedRate(() -> {
//            logger.info("=============broadcastPingMessageFromConsumer============");
            ConsumerConnectionManager.broadcastPingMessageFromConsumer();
        }, 3, heartbeatInterval, TimeUnit.SECONDS);
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public String getLocalIp() {
        return localIp;
    }

    public static RpcConsumer getInstance() {
        return instance;
    }

    public static void setInstance(RpcConsumer instance) {
        RpcConsumer.instance = instance;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public static Map<String, RpcConsumerHandler> getHandlerMap() {
        return handlerMap;
    }

    public static void setHandlerMap(Map<String, RpcConsumerHandler> handlerMap) {
        RpcConsumer.handlerMap = handlerMap;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public int getCurrentConnectRetryTimes() {
        return currentConnectRetryTimes;
    }

    public void setCurrentConnectRetryTimes(int currentConnectRetryTimes) {
        this.currentConnectRetryTimes = currentConnectRetryTimes;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public int getScanNotActiveChannelInterval() {
        return scanNotActiveChannelInterval;
    }

    public boolean isEnableDirectServer() {
        return enableDirectServer;
    }

    public String getDirectServerUrl() {
        return directServerUrl;
    }

    public ConcurrentThreadPool getConcurrentThreadPool() {
        return concurrentThreadPool;
    }

    public void setConcurrentThreadPool(ConcurrentThreadPool concurrentThreadPool) {
        this.concurrentThreadPool = concurrentThreadPool;
    }

    public FlowPostProcessor getFlowPostProcessor() {
        return flowPostProcessor;
    }

    public RpcConsumer setFlowPostProcessor(String flowType){
        if (StringUtils.isEmpty(flowType)){
            flowType = RpcConstants.FLOW_POST_PROCESSOR_PRINT;
        }
        this.flowPostProcessor = ExtensionLoader.getExtension(FlowPostProcessor.class, flowType);
        return this;
    }
}
