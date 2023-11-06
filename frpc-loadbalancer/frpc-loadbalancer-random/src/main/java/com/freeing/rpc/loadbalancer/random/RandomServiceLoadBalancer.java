package com.freeing.rpc.loadbalancer.random;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 基于随机算法的负载均衡策略
 *
 * @author yanggy
 */
@SPIClass
public class RandomServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(RandomServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode) {
        logger.info("select server base RandomServiceLoadBalancer.class");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        int randomIndex = new Random().nextInt(servers.size());
        return servers.get(randomIndex);
    }
}
