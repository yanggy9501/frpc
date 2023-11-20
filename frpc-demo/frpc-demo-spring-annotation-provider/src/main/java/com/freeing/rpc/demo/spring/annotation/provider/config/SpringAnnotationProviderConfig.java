package com.freeing.rpc.demo.spring.annotation.provider.config;

import com.freeing.rpc.provider.spring.RpcSpringServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author yanggy
 */
@Configuration
@ComponentScan(basePackages = "com.freeing.rpc.demo")
@PropertySource(value = {"classpath:rpc.properties"})
public class SpringAnnotationProviderConfig {
    @Value("${registry.address}")
    private String registryAddress;

    @Value("${registry.type}")
    private String registryType;

    @Value("${registry.loadbalance.type}")
    private String registryLoadbalanceType;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${reflect.type}")
    private String reflectType;

    @Value("${server.heartbeatInterval}")
    private int heartbeatInterval;

    @Value("${server.scanNotActiveChannelInterval}")
    private int scanNotActiveChannelInterval;

    @Bean
    public RpcSpringServer rpcSpringServer(){
        return new RpcSpringServer(serverAddress, registryAddress, registryType, registryLoadbalanceType, reflectType, heartbeatInterval, scanNotActiveChannelInterval);
    }
}
