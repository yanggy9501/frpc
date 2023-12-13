package com.freeing.rpc.buffer.object;

import com.freeing.rpc.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * 缓冲对象
 *
 * @author yanggy
 */
public class BufferObject<T> implements Serializable {
    private static final long serialVersionUID = -5465112244213170405L;

    /**
     * Netty读写数据的ChannelHandlerContext
     */
    private ChannelHandlerContext ctx;

    private RpcProtocol<T> protocol;

    public BufferObject(ChannelHandlerContext ctx, RpcProtocol<T> protocol) {
        this.ctx = ctx;
        this.protocol = protocol;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public RpcProtocol<T> getProtocol() {
        return protocol;
    }

    public void setProtocol(RpcProtocol<T> protocol) {
        this.protocol = protocol;
    }
}
