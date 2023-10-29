package com.freeing.rpc.loadbalancer.random;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 基于随机算法的负载均衡策略
 *
 * @author yanggy
 */
public class RandomServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final Logger logger = LoggerFactory.getLogger(RandomServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode) {
        logger.info("使用基于随机选举算法的负载均衡策略");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        int randomIndex = new Random().nextInt(servers.size());
        return servers.get(randomIndex);
    }
}
