package com.freeing.rpc.consumer.common.initializer;

import com.freeing.rpc.codec.RpcDecoder;
import com.freeing.rpc.codec.RpcEncoder;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.consumer.common.handler.RpcConsumerHandler;
import com.freeing.rpc.flow.processor.FlowPostProcessor;
import com.freeing.rpc.threadpool.ConcurrentThreadPool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * RpcConsumerInitializer
 *
 * @author yanggy
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private int heartbeatInterval;

    private ConcurrentThreadPool concurrentThreadPool;

    private FlowPostProcessor flowPostProcessor;

    private boolean enableBuffer;

    private int bufferSize;

    public RpcConsumerInitializer(int heartbeatInterval, boolean enableBuffer,
        int bufferSize, ConcurrentThreadPool concurrentThreadPool,
        FlowPostProcessor flowPostProcessor){
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.concurrentThreadPool = concurrentThreadPool;
        this.flowPostProcessor = flowPostProcessor;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder(flowPostProcessor));
        cp.addLast(RpcConstants.CODEC_DECODER, new RpcDecoder(flowPostProcessor));
        cp.addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER, new IdleStateHandler(heartbeatInterval, 0, 0, TimeUnit.MILLISECONDS));
        cp.addLast(RpcConstants.CODEC_HANDLER, new RpcConsumerHandler(enableBuffer, bufferSize, concurrentThreadPool));
    }
}
