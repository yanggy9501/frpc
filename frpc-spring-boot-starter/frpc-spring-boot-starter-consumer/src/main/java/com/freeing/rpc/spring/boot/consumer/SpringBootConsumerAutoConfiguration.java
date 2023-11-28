package com.freeing.rpc.spring.boot.consumer;

import com.freeing.rpc.consumer.RpcClient;
import com.freeing.rpc.spring.boot.consumer.config.SpringBootConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanggy
 */
@Configuration
@EnableConfigurationProperties
public class SpringBootConsumerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "frpc.consumer")
    public SpringBootConsumerConfig springBootConsumerConfig() {
        return new SpringBootConsumerConfig();
    }

    @Bean
    public RpcClient rpcClient(final SpringBootConsumerConfig springBootConsumerConfig) {
        return new RpcClient(springBootConsumerConfig.getRegistryAddress(),
            springBootConsumerConfig.getRegistryType(),
            springBootConsumerConfig.getLoadBalanceType(),
            springBootConsumerConfig.getProxy(),
            springBootConsumerConfig.getVersion(),
            springBootConsumerConfig.getGroup(),
            springBootConsumerConfig.getSerializationType(),
            springBootConsumerConfig.getTimeout(),
            springBootConsumerConfig.getAsync(),
            springBootConsumerConfig.getOneway(),
            springBootConsumerConfig.getHeartbeatInterval(),
            springBootConsumerConfig.getScanNotActiveChannelInterval(),
            springBootConsumerConfig.getRetryInterval(),
            springBootConsumerConfig.getRetryTimes(),
            springBootConsumerConfig.getEnableResultCache(),
            springBootConsumerConfig.getResultCacheExpire()
            );
    }
}
