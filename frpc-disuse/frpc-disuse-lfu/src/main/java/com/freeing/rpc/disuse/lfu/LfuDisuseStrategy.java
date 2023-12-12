package com.freeing.rpc.disuse.lfu;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 在一段时间内，数据被使用次数最少的，优先被淘汰。
 *
 * @author yanggy
 */
@SPIClass
public class LfuDisuseStrategy implements DisuseStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(LfuDisuseStrategy.class);

    private static final Comparator<ConnectionInfo> useCountComparator = (o1, o2) -> o1.getUseCount() - o2.getUseCount() > 0 ? 1 : -1;

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        LOGGER.info("execute lfu disuse strategy...");
        if (connectionList == null || connectionList.isEmpty()) {
            return null;
        }
        connectionList.sort(useCountComparator);
        return connectionList.get(0);
    }
}
