package com.freeing.rpc.loadbalancer.sourceip.hash;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 基于源IP地址Hash的负载均衡策略
 *
 * @author yanggy
 */
@SPIClass
public class SourceIpHashServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private static final Logger logger = LoggerFactory.getLogger(SourceIpHashServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashcode, String sourceIp) {
        logger.info("select server base SourceIpHashServiceLoadBalancer.class");
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        // 传入的IP地址为空，则默认返回第一个服务实例
        if (StringUtils.isEmpty(sourceIp)) {
            return servers.get(0);
        }
        int resultHashCode = Math.abs(sourceIp.hashCode() + hashcode);
        return servers.get(resultHashCode % servers.size());
    }
}
