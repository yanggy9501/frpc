package com.freeing.rpc.demo.spring.boot.consumer.service.impl;

import com.freeing.rpc.annotation.RpcReference;
import com.freeing.rpc.demo.api.DemoService;
import com.freeing.rpc.demo.spring.boot.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

@Service
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    @RpcReference(registryAddress = "192.168.134.128:2181",
        version = "1.0.0", group = "default",
        proxy = "cglib", timeout = 30000, async = false, oneway = false)
    private DemoService demoService;

    @Override
    public String hello(String name) {
        return demoService.hello(name);
    }
}
