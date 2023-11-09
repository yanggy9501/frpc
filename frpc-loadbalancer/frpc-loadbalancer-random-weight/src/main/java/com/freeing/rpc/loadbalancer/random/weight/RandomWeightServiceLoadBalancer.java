package com.freeing.rpc.loadbalancer.random.weight;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 加权随机
 *
 * @author yanggy
 */
@SPIClass
public class RandomWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(RandomWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base RandomWeightServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        hashcode = Math.abs(hashcode);
        int count = hashcode % servers.size();
        if (count <= 1){
            count = servers.size();
        }
        Random random = new Random();
        int index = random.nextInt(count);
        return servers.get(index);
    }
}
