package com.freeing.rpc.spring.boot.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yanggy
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.freeing.rpc")
public class SpringBootProviderDemoStarter {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProviderDemoStarter.class, args);
    }
}
