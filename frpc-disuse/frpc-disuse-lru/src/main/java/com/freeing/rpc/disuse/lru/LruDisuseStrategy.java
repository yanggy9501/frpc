package com.freeing.rpc.disuse.lru;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 判断最近被使用的时间，目前最远的数据优先被淘汰。
 *
 * @author yanggy
 */
@SPIClass
public class LruDisuseStrategy implements DisuseStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LruDisuseStrategy.class);

    private static Comparator<ConnectionInfo> lastUseTimeComparator = (o1, o2) -> o1.getLastUseTime() - o2.getLastUseTime() > 0 ? 1 : -1;

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute lru disuse strategy...");
        if (connectionList == null || connectionList.isEmpty()) {
            return null;
        }
        connectionList.sort(lastUseTimeComparator);
        return connectionList.get(0);
    }
}
