package com.freeing.loadbalancer.api;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.spi.annotation.SPI;

import java.util.List;

/**
 * 负载均衡
 */
@SPI(RpcConstants.SERVICE_LOAD_BALANCER_RANDOM)
public interface ServiceLoadBalancer<T> {

    /**
     * 以负载均衡的方式选取一个服务节点
     *
     * @param servers 服务列表
     * @param hashcode Hash值
     * @return 可用的服务节点
     */
    T select(List<T> servers, int hashcode);
}
