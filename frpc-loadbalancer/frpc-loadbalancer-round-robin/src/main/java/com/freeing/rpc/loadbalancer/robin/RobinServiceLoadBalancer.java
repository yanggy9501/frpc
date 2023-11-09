package com.freeing.rpc.loadbalancer.robin;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于轮询算法的负载均衡策略
 *
 * @author yanggy
 */
@SPIClass
public class RobinServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(RobinServiceLoadBalancer.class);

    private final AtomicInteger nextIndex = new AtomicInteger();

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base RobinServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        int len = servers.size();
        int index = nextIndex.getAndIncrement();
        if (index < 0) {
            nextIndex.set(0);
            index = nextIndex.getAndIncrement();
        }
        return servers.get(index % len);
    }
}
