package com.freeing.rpc.loadbalancer.sourceip.hash.weight;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * 基于源IP地址加权Hash的负载均衡策略
 *
 * @author yanggy
 */
@SPIClass
public class SourceIpHashWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(SourceIpHashWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base SourceIpHashWeightServiceLoadBalancer.class");
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        if (StringUtils.isEmpty(sourceIp)) {
            return servers.get(0);
        }
        int count = Math.abs(hashcode) % servers.size();
        if (count <= 0) {
            count = servers.size();
        }

        int resultHashCode = Math.abs(sourceIp.hashCode() + hashcode);
        return servers.get(resultHashCode % count);
    }
}
