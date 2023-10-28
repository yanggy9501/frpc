package com.freeing.rpc.test.registry;

import com.freeing.rpc.protocol.meta.ServiceMeta;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yanggy
 */
public class ZookeeperRegistryTest {

    private RegistryService registryService;

    private ServiceMeta serviceMeta;

    @Before
    public void init() throws Exception {
        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper");
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest.class.getName(),
            "1.0.0",
            "default",
            "127.0.0.1",
            8080);
    }

    @Test
    public void testRegister() throws Exception {
        this.registryService.register(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception {
        ServiceMeta discovery = this.registryService.discovery(RegistryService.class.getName(), "default".hashCode());
        System.out.println(discovery);
    }
}
