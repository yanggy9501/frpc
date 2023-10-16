package com.freeing.rpc.test.provider.service.impl;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.test.provider.service.DemoService;

/**
 * @author yanggy
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "com.freeing.rpc.test.provider.service.DemoService", version = "1.0.0", group = "freeing")
public class ProviderDemoServiceImpl {
}
