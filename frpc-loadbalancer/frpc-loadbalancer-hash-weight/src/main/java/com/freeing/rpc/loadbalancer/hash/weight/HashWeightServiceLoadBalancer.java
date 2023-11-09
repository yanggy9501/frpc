package com.freeing.rpc.loadbalancer.hash.weight;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * 于加权Hash算法负载均衡策略
 *
 * @author yanggy
 */
@SPIClass
public class HashWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(HashWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base HashWeightServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        hashcode = Math.abs(hashcode);
        int count = hashcode % servers.size();
        if (count <= 0) {
            count = servers.size();
        }
        int index = hashcode % count;
        return servers.get(index);
    }
}
