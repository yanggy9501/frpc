package com.freeing.rpc.connection.manager;

import com.freeing.rpc.common.exception.RefuseException;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接管理器
 *
 * @author yanggy
 */
public class ConnectionManager {
    private static volatile ConnectionManager instance;

    private final Map<String, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();
    private final DisuseStrategy disuseStrategy;
    private final int maxConnections;

    private ConnectionManager(int maxConnections, String disuseStrategyType){
        this.maxConnections = maxConnections <= 0 ? Integer.MAX_VALUE : maxConnections;
        disuseStrategyType = StringUtils.isEmpty(disuseStrategyType) ? RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT : disuseStrategyType;
        this.disuseStrategy = ExtensionLoader.getExtension(DisuseStrategy.class, disuseStrategyType);
    }

    public static ConnectionManager getInstance(int maxConnections, String disuseStrategyType){
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager(maxConnections, disuseStrategyType);
                }
            }
        }
        return instance;
    }

    public void add(Channel channel) {
        ConnectionInfo connectionInfo = new ConnectionInfo(channel);
        if (this.checkConnectionList(connectionInfo)) {
            connectionMap.put(getKey(channel), connectionInfo);
        }
    }

    /**
     * 移除连接
     */
    public void remove(Channel channel){
        connectionMap.remove(getKey(channel));
    }

    /**
     * 更新连接信息
     */
    public void update(Channel channel){
        ConnectionInfo info = connectionMap.get(getKey(channel));
        info.setLastUseTime(System.currentTimeMillis());
        info.incrementUseCount();
        connectionMap.put(getKey(channel), info);
    }

    /**
     * 检测连接列表
     */
    private boolean checkConnectionList(ConnectionInfo info) {
        List<ConnectionInfo> connectionList = new ArrayList<>(connectionMap.values());
        if (connectionList.size() >= maxConnections){
            try{
                ConnectionInfo cacheConnectionInfo = disuseStrategy.selectConnection(connectionList);
                if (cacheConnectionInfo != null){
                    cacheConnectionInfo.getChannel().close();
                    connectionMap.remove(getKey(cacheConnectionInfo.getChannel()));
                }
            }catch (RefuseException e){
                info.getChannel().close();
                return false;
            }
        }
        return true;
    }

    private String getKey(Channel channel) {
        return channel.id().asLongText();
    }
}
