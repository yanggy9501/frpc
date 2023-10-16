package com.freeing.rpc.common.scanner.test.provider;

import com.freeing.rpc.annotation.RpcService;
import com.freeing.rpc.common.scanner.test.service.DemoService;

/**
 * @author yanggy
 */
@RpcService(interfaceClass = DemoService.class,
    interfaceClassName = "com.freeing.rpc.common.scanner.test.service.DemoService",
    version = "1.0.0", group = "freeing")
public class ProviderDemoServiceImpl implements DemoService {

}
