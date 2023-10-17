package com.freeing.rpc.protocol.enumeration;

/**
 *  RPC服务状态
 *
 * @author yanggy
 */
public enum RpcStatus {
    SUCCESS(0),
    FAIL(1),
    ;
    private final int code;

    RpcStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
