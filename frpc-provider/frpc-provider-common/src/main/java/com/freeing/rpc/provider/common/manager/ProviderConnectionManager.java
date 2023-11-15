package com.freeing.rpc.provider.common.manager;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.response.RpcResponse;
import com.freeing.rpc.provider.common.cache.ProviderChannelCache;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 服务提供者连接管理器
 *
 * @author yanggy
 */
public class ProviderConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ProviderConnectionManager.class);

    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        for (Channel channel : channelCache) {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ProviderChannelCache.remove(channel);
            }
        }
    }

    /**
     * 发送ping消息
     */
    public static  void broadcastPingMessageFromProvider() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }

        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_JDK,
                                                            RpcType.HEARTBEAT_FROM_PROVIDER.getType());
        RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(RpcConstants.HEARTBEAT_PING);
        protocol.setHeader(header);
        protocol.setBody(rpcResponse);
        for (Channel channel : channelCache) {
            if (channel.isOpen() && channel.isActive()) {
                channel.writeAndFlush(protocol);
            }
        }
    }

}
