package com.freeing.rpc.disuse.api;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPI;

import java.util.List;

/**
 * 淘汰策略
 *
 * @author yanggy
 */
@SPI(RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT)
public interface DisuseStrategy {

    /**
     * 从连接列表中根据规则获取一个连接对象
     */
    ConnectionInfo selectConnection(List<ConnectionInfo> connectionList);
}
