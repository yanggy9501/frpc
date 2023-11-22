package com.freeing.rpc.demo.spring.annotation;

import com.freeing.rpc.demo.api.DemoService;
import com.freeing.rpc.demo.spring.annotation.config.SpringAnnotationConsumerConfig;
import com.freeing.rpc.demo.spring.annotation.service.SpringAnnotationConsumerTestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author yanggy
 */
public class SpringAnnotationConsumerTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationConsumerConfig.class);
        SpringAnnotationConsumerTestService service = context.getBean(SpringAnnotationConsumerTestService.class);
        DemoService demoService = service.getDemoService();
        String result = service.getDemoService().hello("kato");
        System.out.println("返回的结果数据===>>> " + result);
    }
}
