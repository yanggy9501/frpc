package com.freeing.rpc.consumer.common;

import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.common.threadpool.ClientThreadPool;
import com.freeing.rpc.common.utils.ip.IpUtils;
import com.freeing.rpc.consumer.common.handler.RpcConsumerHandler;
import com.freeing.rpc.consumer.common.initializer.RpcConsumerInitializer;
import com.freeing.rpc.consumer.common.manager.ConsumerConnectionManager;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.consumer.Consumer;
import com.freeing.rpc.proxy.api.future.RPCFuture;
import com.freeing.rpc.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 心跳间隔时间，默认30秒
     */
    private int heartbeatInterval = 30000;

    /**
     * 扫描并移除空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval = 60000;

    private RpcConsumer(int heartbeatInterval, int scanNotActiveChannelInterval) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }

        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new RpcConsumerInitializer());
        // TODO 启动心跳，后续优化
        this.startHeartbeat();
    }

    public static RpcConsumer getInstance(int heartbeatInterval, int scanNotActiveChannelInterval) {
        if (instance == null){
            synchronized (RpcConsumer.class){
                if (instance == null){
                    instance = new RpcConsumer(heartbeatInterval, scanNotActiveChannelInterval);
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
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
        if (Objects.nonNull(serviceMeta)) {
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            if (Objects.isNull(handler)) {
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            } else if (!handler.getChannel().isActive()) {
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            }
            return handler.sendRequest(protocol, request.getAsync(), request.getOneway());
        }
        return null;
    }

    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();
        channelFuture.addListener((ChannelFutureListener) -> {
            if (channelFuture.isSuccess()) {
                logger.info("connect rpc server {} on port {} success.", serviceAddress, port);
            } else {
                logger.error("connect rpc server {} on port {} failed.", serviceAddress, port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
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
}
