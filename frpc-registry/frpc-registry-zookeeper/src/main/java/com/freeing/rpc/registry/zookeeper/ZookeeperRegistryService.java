package com.freeing.rpc.registry.zookeeper;

import com.freeing.loadbalancer.api.ServiceLoadBalancer;
import com.freeing.loadbalancer.helper.ServiceLoadBalancerHelper;
import com.freeing.rpc.common.helper.RpcServiceHelper;
import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.spi.annotation.SPIClass;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 基于Zookeeper的注册服务
 *
 * @author yanggy
 */
@SPIClass
public class ZookeeperRegistryService implements RegistryService {

    public static final int BASE_SLEEP_TIME_MS = 100000;

    public static final int MAX_RETRIES = 3;

    public static final String ZK_BASE_PATH = "/frpc";

    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;

    private ServiceLoadBalancer<ServiceMeta> serviceEnhancedLoadBalancer;

    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(registryConfig.getRegistryAddr())
            .retryPolicy(new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES))
            .build();
        client.start();

        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
            .client(client)
            .serializer(serializer)
            .basePath(ZK_BASE_PATH)
            .build();
        this.serviceDiscovery.start();
        // SPI 负载均衡
        if (registryConfig.getRegistryLoadBalanceType().toLowerCase()
                .contains(RpcConstants.SERVICE_ENHANCED_LOAD_BALANCER_PREFIX)) {
            this.serviceEnhancedLoadBalancer =
                ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.getRegistryLoadBalanceType());
        } else {
            this.serviceLoadBalancer =
                ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.getRegistryLoadBalanceType());
        }
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
            .<ServiceMeta>builder()
            .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
            .address(serviceMeta.getServiceAddr())
            .port(serviceMeta.getServicePort())
            .payload(serviceMeta)
            .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
            .<ServiceMeta>builder()
            .name(serviceMeta.getServiceName())
            .address(serviceMeta.getServiceAddr())
            .port(serviceMeta.getServicePort())
            .payload(serviceMeta)
            .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);

        if (Objects.nonNull(serviceLoadBalancer)) {
            return getServiceMetaInstance(invokerHashCode, sourceIp, (List<ServiceInstance<ServiceMeta>>) serviceInstances);
        }
        return this.serviceEnhancedLoadBalancer.select(ServiceLoadBalancerHelper
            .getServiceMetaList((List<ServiceInstance<ServiceMeta>>) serviceInstances), invokerHashCode, sourceIp);
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }

    private ServiceMeta getServiceMetaInstance(int invokerHashCode, String sourceIp, List<ServiceInstance<ServiceMeta>> serviceInstances) {
        ServiceInstance<ServiceMeta> instance = this.serviceLoadBalancer.select(serviceInstances, invokerHashCode, sourceIp);
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }
}
