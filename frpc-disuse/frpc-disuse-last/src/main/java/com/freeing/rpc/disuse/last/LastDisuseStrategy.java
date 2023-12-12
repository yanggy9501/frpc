package com.freeing.rpc.disuse.last;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 获取连接列表中最后一个连接信息
 *
 * @author yanggy
 */
@SPIClass
public class LastDisuseStrategy implements DisuseStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LastDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute last disuse strategy...");
        if (connectionList == null || connectionList.isEmpty()) {
            return null;
        }
        return connectionList.get(connectionList.size() - 1);
    }
}
