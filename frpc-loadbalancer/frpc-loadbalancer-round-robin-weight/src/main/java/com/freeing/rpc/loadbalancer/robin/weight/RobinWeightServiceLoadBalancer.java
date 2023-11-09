package com.freeing.rpc.loadbalancer.robin.weight;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yanggy
 */
@SPIClass
public class RobinWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(RobinWeightServiceLoadBalancer.class);

    private final AtomicInteger next_index = new AtomicInteger(0);

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base RobinWeightServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        hashcode = Math.abs(hashcode);
        int count = hashcode % servers.size();
        if (count <= 0){
            count = servers.size();
        }
        int index = next_index.incrementAndGet();
        if (index >= Integer.MAX_VALUE - 10000){
            next_index.set(0);
        }
        return servers.get(index % count);
    }
}
