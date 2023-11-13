package com.freeing.rpc.consumer.common.manager;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.consumer.common.cache.ConsumerChannelCache;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.request.RpcRequest;
import io.netty.channel.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 服务消费者连接管理器
 *
 * @author yanggy
 */
public class ConsumerConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerConnectionManager.class);

    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNoTActiveChannel() {
        Set<Channel> channelSet = ConsumerChannelCache.getChannelCache();
        if (CollectionUtils.isEmpty(channelSet)) {
            return;
        }
        for (Channel channel : channelSet) {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ConsumerChannelCache.remove(channel);
            }
        }
    }

    /**
     * 广播 ping 消息
     */
    public static void broadcastPingMessageFromConsumer() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (CollectionUtils.isEmpty(channelCache)) {
            return;
        }
        // 请求头
        RpcHeader header = RpcHeaderFactory
            .getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());

        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        protocol.setHeader(header);
        protocol.setBody(request);

        for (Channel channel : channelCache) {
            if (channel.isOpen() && channel.isActive()) {
                logger.info("send heartbeat message to service provider, the provider is: {}, the heartbeat message is: {}",
                    channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
                channel.writeAndFlush(channel);
            }
        }
    }
}
