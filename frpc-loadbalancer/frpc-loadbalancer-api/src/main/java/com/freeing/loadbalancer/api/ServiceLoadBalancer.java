package com.freeing.loadbalancer.api;

import java.util.List;

/**
 * 负载均衡
 */
public interface ServiceLoadBalancer<T> {

    T select(List<T> servers, int hashcode);
}
