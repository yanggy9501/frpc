package com.freeing.rpc.test.provider.service.impl;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.test.provider.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yanggy
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "com.freeing.rpc.test.provider.service.DemoService", version = "1.0.0", group = "freeing")
public class ProviderDemoServiceImpl implements DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);
    @Override
    public String hello(String name) {
        LOGGER.info("调用 hello 方法，方法参数 {}", name);
        return "hello " + name;
    }
}
