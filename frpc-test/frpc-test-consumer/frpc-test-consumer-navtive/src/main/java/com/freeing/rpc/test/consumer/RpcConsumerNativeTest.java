package com.freeing.rpc.test.consumer;

import com.freeing.rpc.consumer.RpcClient;
import com.freeing.rpc.test.api.DemoService;

/**
 * 测试Java原生启动服务消费者
 *
 * @author yanggy
 */
public class RpcConsumerNativeTest {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper",
            "1.0.0",
            "default", "kryo", 3000, false, false);

        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("katou");
        System.out.println("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
    }
}
