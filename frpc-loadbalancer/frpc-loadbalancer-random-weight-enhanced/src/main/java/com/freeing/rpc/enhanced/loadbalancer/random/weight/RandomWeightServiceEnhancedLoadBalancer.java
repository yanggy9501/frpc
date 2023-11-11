package com.freeing.rpc.enhanced.loadbalancer.random.weight;

import com.freeing.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * 增强型加权随机
 *
 * @author yanggy
 */
@SPIClass
public class RandomWeightServiceEnhancedLoadBalancer extends BaseEnhancedServiceLoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(RandomWeightServiceEnhancedLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashcode, String sourceIp) {
        logger.info("select server base RandomWeightServiceEnhancedLoadBalancer.class");
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        servers = getWeightServiceMetaList(servers);
        Random random = new Random();
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
