package com.freeing.rpc.consumer.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 缓存连接成功的Channel
 *
 * @author yanggy
 */
public class ConsumerChannelCache {

    private static final Set<Channel> channelSet = new CopyOnWriteArraySet<>();

    public static void add(Channel channel) {
        channelSet.add(channel);
    }

    public static void remove(Channel channel) {
        channelSet.remove(channel);
    }

    public static Set<Channel> getChannelCache() {
        return channelSet;
    }
}
