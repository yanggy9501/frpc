package com.freeing.rpc.disuse.defaultstrategy;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  默认连接，获取列表中的第一个元素
 *
 * @author yanggy
 */
@SPIClass
public class DefaultDisuseStrategy implements DisuseStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute default disuse strategy ...");
        if (connectionList == null && connectionList.isEmpty()) {
            return null;
        }
        return connectionList.get(0);
    }
}
