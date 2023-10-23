package com.freeing.rpc.test.provider.service.impl;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yanggy
 */
@RpcService(interfaceClass = DemoService.class, version = "1.0.0", group = "default")
public class ProviderDemoServiceImpl implements DemoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        LOGGER.info("调用 hello 方法，方法参数 {}", name);
        return "hello " + name;
    }
}
