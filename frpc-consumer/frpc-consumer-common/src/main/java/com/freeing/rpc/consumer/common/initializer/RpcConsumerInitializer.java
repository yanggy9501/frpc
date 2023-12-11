package com.freeing.rpc.consumer.common.initializer;

import com.freeing.rpc.codec.RpcDecoder;
import com.freeing.rpc.codec.RpcEncoder;
import com.freeing.rpc.consumer.common.handler.RpcConsumerHandler;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.threadpool.ConcurrentThreadPool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * RpcConsumerInitializer
 *
 * @author yanggy
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private int heartbeatInterval;

    private ConcurrentThreadPool concurrentThreadPool;

    private FlowPostProcessor flowPostProcessor;

    public RpcConsumerInitializer(int heartbeatInterval, ConcurrentThreadPool concurrentThreadPool,
        FlowPostProcessor flowPostProcessor) {
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.concurrentThreadPool = concurrentThreadPool;
        this.flowPostProcessor = flowPostProcessor;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new RpcEncoder(flowPostProcessor));
        pipeline.addLast(new RpcDecoder(flowPostProcessor));
        pipeline.addLast(new RpcConsumerHandler());
    }
}
