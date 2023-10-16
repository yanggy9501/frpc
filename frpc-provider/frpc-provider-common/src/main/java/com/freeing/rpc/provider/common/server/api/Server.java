package com.freeing.rpc.provider.common.server.api;

/**
 * 启动RPC服务的接口
 *
 * @author yanggy
 */
public interface Server {
    /**
     *  启动Netty服务
     */
    void startNettyServer();
}
