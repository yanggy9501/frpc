package com.freeing.rpc.protocol.base;

import java.io.Serializable;

/**
 * 消息基类
 *
 * @author yanggy
 */
public class RpcMessage implements Serializable {
    /**
     * 是否单向发送
     */
    private boolean oneway;

    /**
     * 是否异步调用
     */
    private boolean async;

    public boolean getOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean asynce) {
        this.async = asynce;
    }
}
