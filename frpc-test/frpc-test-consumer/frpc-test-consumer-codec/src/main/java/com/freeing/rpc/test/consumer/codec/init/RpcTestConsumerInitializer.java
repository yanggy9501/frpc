package com.freeing.rpc.test.consumer.codec.init;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author yanggy
 */
public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
//        ChannelPipeline cp = socketChannel.pipeline();
//        cp.addLast(new RpcEncoder());
//        cp.addLast(new RpcDecoder());
//        cp.addLast(new RpcTestConsumerHandler());
    }
}
