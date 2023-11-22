package com.freeing.rpc.demo.spring.xml.counsumer;

import com.freeing.rpc.consumer.RpcClient;
import com.freeing.rpc.demo.api.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author yanggy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class SpringXmlConsumerTest {

    @Autowired
    private RpcClient rpcClient;

   @Test
    public void test() {
       DemoService demoService = rpcClient.create(DemoService.class);
       String result = demoService.hello("kato");
       System.out.println("返回的结果数据===>>> " + result);
   }
}
