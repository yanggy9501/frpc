package com.freeing.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.consumer.common.cache.ConsumerChannelCache;
import com.freeing.rpc.consumer.common.context.RpcContext;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.enumeration.RpcStatus;
import com.freeing.rpc.protocol.enumeration.RpcType;
import com.freeing.rpc.protocol.header.RpcHeader;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import com.freeing.rpc.proxy.api.future.RPCFuture;
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
     * 存储请求ID与RRPCFuture的映射关系
     */
    private Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.add(channel);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol)
            throws Exception {
        if (Objects.isNull(protocol)) {
            return;
        }
        logger.info("服务消费者收到服务过提供者的数据 ===>>> {}", JSON.toJSONString(protocol));
        handlerMessage(protocol, channelHandlerContext.channel());
    }

    private void handlerMessage(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        // 服务提供者响应的心跳消息
        if (header.getMsgType() == RpcType.HEARTBEAT_TO_CONSUMER.getType()) {
            handlerHeartbeatMessageToConsumer(protocol);
        }
        else if(header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_PROVIDER.getType()) {
            this.handlerHeartbeatMessageFromProvider(protocol, channel);
        }
        // 服务器响应数据
        else if (header.getMsgType() == RpcType.RESPONSE.getType()) {
            handlerResponseMessage(protocol, header);
        }
    }

    private void handlerHeartbeatMessageFromProvider(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_PROVIDER.getType());
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PONG});
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(request);
        channel.writeAndFlush(requestRpcProtocol);
    }

    private void handlerHeartbeatMessageToConsumer(RpcProtocol<RpcResponse> protocol) {
        // 此处简单打印即可 ,实际场景可不做处理
        logger.info("receive service provider heartbeat message: {}", protocol.getBody().getResult());
    }

    private void handlerResponseMessage(RpcProtocol<RpcResponse> protocol, RpcHeader header) {
        long requestId = header.getRequestId();
        RPCFuture rpcFuture = pendingRPC.remove(requestId);
        if (rpcFuture != null){
            rpcFuture.done(protocol);
        }
    }

    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, boolean async, boolean oneway) {
        RPCFuture rpcFuture;
        if (oneway) {
            rpcFuture = sendRequestOneway(protocol);
        } else if (async) {
            rpcFuture = sendRequestAsync(protocol);
        } else {
            rpcFuture = sendRequestSync(protocol);
        }
        return rpcFuture;
    }

    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        logger.info("服务消费者 Sync 方式发送数据 ===>>> {}", JSON.toJSONString(protocol));
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        logger.info("服务消费者 Async 方式发送数据 ===>>> {}", JSON.toJSONString(protocol));
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        // 如果是异步调用，则将RPCFuture放入RpcContext
        RpcContext.getContext().setRPCFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        logger.info("服务消费者 Oneway 方式发送数据 ===>>> {}", JSON.toJSONString(protocol));
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = new RPCFuture(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRPC.put(requestId, rpcFuture);
        return rpcFuture;
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
