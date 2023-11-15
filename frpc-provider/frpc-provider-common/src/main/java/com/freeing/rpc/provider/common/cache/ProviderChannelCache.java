package com.freeing.rpc.provider.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 服务提供者缓存连接成功的Channel
 *
 * @author yanggy
 */
public class ProviderChannelCache {
    private static final Set<Channel> CHANNEL_SET = new CopyOnWriteArraySet<>();

    public static void add(io.netty.channel.Channel channel){
        CHANNEL_SET.add(channel);
    }

    public static void remove(io.netty.channel.Channel channel){
        CHANNEL_SET.remove(channel);
    }

    public static Set<io.netty.channel.Channel> getChannelCache(){
        return CHANNEL_SET;
    }
}
