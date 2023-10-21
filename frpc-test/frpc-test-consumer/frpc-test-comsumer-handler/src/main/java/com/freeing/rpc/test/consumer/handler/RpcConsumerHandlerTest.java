package com.freeing.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSON;
import com.freeing.rpc.consumer.common.RpcConsumer;
import com.freeing.rpc.consumer.common.future.RPCFuture;
import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.header.RpcHeaderFactory;
import com.freeing.rpc.protocol.request.RpcRequest;

/**
 * @author yanggy
 */
public class RpcConsumerHandlerTest {
    public static void main(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance();
        RPCFuture result = consumer.sendRequest(getRpcRequestProtocol());
        Thread.sleep(2000);
        System.out.println(JSON.toJSONString(result.get()));
        consumer.close();
    }


    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest request = new RpcRequest();
        request.setClassName("com.freeing.rpc.test.provider.service.DemoService");
        request.setGroup("freeing");
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
