package com.freeing.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者处理器
 *
 * @author yanggy
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private static final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;

    private SocketAddress remotePeer;

    /**
     * 存储请求ID与RpcResponse协议的映射关系
     */
    private Map<Long, RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        if (Objects.isNull(protocol)) {
            return;
        }
        logger.info("服务消费者接收到的数据 ===>>> {}", JSON.toJSONString(protocol));
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingResponse.put(requestId, protocol);
    }

    public Object sendRequest(RpcProtocol<RpcRequest> protocol) {
        logger.info("服务消费值发送数据 ===>>> {}", JSON.toJSONString(protocol));
        channel.writeAndFlush(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        // 异步转同步
        while (true) {
            RpcProtocol<RpcResponse> responseRpcProtocol = pendingResponse.remove(requestId);
            if (Objects.nonNull(responseRpcProtocol)) {
                return responseRpcProtocol;
            }
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    public void setRemotePeer(SocketAddress remotePeer) {
        this.remotePeer = remotePeer;
    }
}
