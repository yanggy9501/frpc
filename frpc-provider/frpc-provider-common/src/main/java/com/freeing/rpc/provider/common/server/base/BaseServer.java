package com.freeing.rpc.provider.common.server.base;

import com.freeing.rpc.codec.RpcDecoder;
import com.freeing.rpc.codec.RpcEncoder;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.provider.common.handler.RpcProviderHandler;
import com.freeing.rpc.provider.common.manager.ProviderConnectionManager;
import com.freeing.rpc.provider.common.server.api.Server;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yanggy
 */
public class BaseServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    /**
     * 默认直接IP
     */
    protected String host = "127.0.0.1";

    /**
     * 默认端口号
     */
    protected int port = 27110;

    /**
     * 存储的是实体类关系
     */
    protected Map<String, Object> handlerMap = new HashMap<>();

    private String reflectType;

    /**
     * 心跳定时任务线程池
     */
    private ScheduledExecutorService executorService;

    /**
     * 注册服务
     */
    protected RegistryService registryService;

    /**
     * 心跳间隔时间，默认30秒
     */
    private int heartbeatInterval = 30000;

    /**
     * 扫描并移除空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval = 60000;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 结果缓存过期时长，默认5秒
     */
    private int resultCacheExpire = 5000;

    /**
     * 流控分析后置处理器
     */
    private FlowPostProcessor flowPostProcessor;

    /**
     * 最大连接限制
     */
    private int maxConnections;

    /**
     * 拒绝策略类型
     */
    private String disuseStrategyType;

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 限流类型
     */
    private String rateLimiterType;

    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    private int permits;

    /**
     * 毫秒数
     */
    private int milliSeconds;

    /**
     * 当限流失败时的处理策略
     */
    private String rateLimiterFailStrategy;

    public BaseServer(String serverAddress, String registryAddress, String registryType,
        String registryLoadBalanceType, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval,
        boolean enableResultCache, int resultCacheExpire, String flowType,
        int maxConnections, String disuseStrategyType, boolean enableBuffer, int bufferSize,
        String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
        if (resultCacheExpire > 0) {
            this.resultCacheExpire = resultCacheExpire;
        }
        this.enableResultCache = enableResultCache;
        this.flowPostProcessor = ExtensionLoader.getExtension(FlowPostProcessor.class, flowType);
        this.maxConnections = maxConnections;
        this.disuseStrategyType = disuseStrategyType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        RegistryService registryService = null;
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        }catch (Exception e){
            logger.error("RPC Server init error", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
         this.startHeartbeat();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            logger.info("RPC Server provider start.");
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                            .addLast(RpcConstants.CODEC_DECODER, new RpcDecoder(flowPostProcessor))
                            .addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder(flowPostProcessor))
                            .addLast(RpcConstants.CODEC_SERVER_IDLE_HANDLER,
                                new IdleStateHandler(0, 0, heartbeatInterval, TimeUnit.MILLISECONDS))
                            .addLast(RpcConstants.CODEC_HANDLER,
                                new RpcProviderHandler(reflectType, handlerMap, enableResultCache, resultCacheExpire,
                                    maxConnections, disuseStrategyType, enableBuffer, bufferSize,
                                    rateLimiterType, permits, milliSeconds, rateLimiterFailStrategy));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e){
            logger.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(() -> {
            logger.info("=============scanNotActiveChannel============");
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(()->{
            logger.info("=============broadcastPingMessageFromProvoder============");
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }
}
