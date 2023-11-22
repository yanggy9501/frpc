package com.freeing.rpc.demo.spring.annotation.service;

import com.freeing.rpc.annotation.RpcReference;
import com.freeing.rpc.demo.api.DemoService;
import org.springframework.stereotype.Component;

/**
 * @author yanggy
 */
@Component
public class SpringAnnotationConsumerTestService {

    @RpcReference(registryType = "zookeeper", registryAddress = "192.168.134.128:2181", loadBalanceType = "random", version = "1.0.0", group = "default", serializationType = "protostuff", proxy = "cglib", timeout = 30000, async = false, oneway = false)
    private DemoService demoService;

    public DemoService getDemoService() {
        return demoService;
    }
}
