package com.freeing.rpc.demo.spring.annotation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanggy
 */
@Configuration
@ComponentScan(value = {"com.freeing.rpc.*"})
public class SpringAnnotationConsumerConfig {

}
