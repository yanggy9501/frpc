package com.freeing.rpc.disuse.fifo;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 判断被存储的时间，离目前最远的数据优先被淘汰。
 *
 * @author yanggy
 */
@SPIClass
public class FifoDisuseStrategy implements DisuseStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(FifoDisuseStrategy.class);

    private final Comparator<ConnectionInfo> connectionTimeComparator = (o1, o2) -> o1.getConnectionTime() - o2.getConnectionTime() > 0 ? 1 : -1;
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        LOGGER.info("execute fifo disuse strategy...");
        if (connectionList == null || connectionList.isEmpty()) {
            return null;
        }
        connectionList.sort(connectionTimeComparator);
        return connectionList.get(0);
    }


}
