package com.freeing.rpc.protocol.response;

import com.freeing.rpc.protocol.base.RpcMessage;

/**
 * @author yanggy
 */
public class RpcResponse extends RpcMessage {
    /**
     * 错误消息
     */
    private String error;

    /**
     * 响应结果
     */
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
