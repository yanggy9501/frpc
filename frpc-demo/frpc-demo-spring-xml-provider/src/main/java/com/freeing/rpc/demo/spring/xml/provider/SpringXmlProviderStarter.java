package com.freeing.rpc.demo.spring.xml.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *  服务提供者启动类
 *
 * @author yanggy
 */
public class SpringXmlProviderStarter {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
