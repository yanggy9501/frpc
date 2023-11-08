package com.freeing.rpc.loadbalacer.hash;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author yanggy
 */
@SPIClass
public class HashServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final Logger logger = LoggerFactory.getLogger(HashServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode) {
        logger.info("select server base HashServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        int index = Math.abs(hashcode) % servers.size();
        return servers.get(index);
    }
}
