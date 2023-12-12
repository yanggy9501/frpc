package com.freeing.rpc.disuse.random;

import com.freeing.rpc.disuse.api.DisuseStrategy;
import com.freeing.rpc.disuse.api.connection.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 从列表中随机获取一个
 *
 * @author yanggy
 */
public class RandomDisuseStrategy implements DisuseStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute random disuse strategy...");
        if (connectionList == null || connectionList.isEmpty()) {
            return null;
        }
        return connectionList.get(new Random().nextInt(connectionList.size()));
    }
}
