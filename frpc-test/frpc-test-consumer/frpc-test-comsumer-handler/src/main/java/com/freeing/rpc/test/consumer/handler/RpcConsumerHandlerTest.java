package com.freeing.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.common.exception.RegistryException;
import com.freeing.rpc.consumer.common.RpcConsumer;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.proxy.api.callback.AsyncRPCCallback;
import com.freeing.rpc.proxy.api.future.RPCFuture;
import com.freeing.rpc.registry.api.RegistryService;
import com.freeing.rpc.registry.api.config.RegistryConfig;
import com.freeing.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yanggy
 */
public class RpcConsumerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);

    public static void main(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance();
        RPCFuture future = consumer.sendRequest(getRpcRequestProtocol(),
            getRegistryService("192.168.134.128:2181",
                "zookeeper",
                "random"));
        future.addCallback(new AsyncRPCCallback() {
            @Override
            public void onSuccess(Object result) {
                LOGGER.info("AsyncRPCCallback#onSuccess|从服务消费者获取到数据|执行回调");
            }

            @Override
            public void onException(Exception e) {
                LOGGER.info("AsyncRPCCallback#onException|rpc 异常");
            }
        });
        Thread.sleep(2000);
        System.out.println(JSON.toJSONString(future.get()));
        consumer.close();
    }

    private static RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registry type cant not be empty");
        }

        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }


    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest request = new RpcRequest();
        request.setClassName("com.freeing.rpc.test.api.DemoService");
        request.setGroup("default");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"hello rpc"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }
}
