package com.freeing.rpc.protocol.enumeration;

/**
 * 消息的类型
 */
public enum RpcType {
    REQUEST(1),

    RESPONSE(2),

    HEARTBEAT(3)
    ;

    private final int type;

    RpcType(int type) {
        this.type = type;
    }

    public static RpcType findByType(int type) {
        for (RpcType rpcType : RpcType.values()) {
            if (rpcType.getType() == type) {
                return rpcType;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }
}
