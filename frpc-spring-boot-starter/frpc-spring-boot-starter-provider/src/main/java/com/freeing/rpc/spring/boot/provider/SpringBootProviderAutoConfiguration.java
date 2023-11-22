package com.freeing.rpc.spring.boot.provider;

import com.freeing.rpc.provider.spring.RpcSpringServer;
import com.freeing.rpc.spring.boot.provider.config.SpringBootProviderConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanggy
 */
@Configuration
public class SpringBootProviderAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "frpc.provider")
    public SpringBootProviderConfig springBootProviderConfig() {
        return new SpringBootProviderConfig();
    }

    @Bean
    public RpcSpringServer rpcSpringServer(final SpringBootProviderConfig springBootProviderConfig) {
        return new RpcSpringServer(springBootProviderConfig.getServerAddress(),
            springBootProviderConfig.getRegistryAddress(),
            springBootProviderConfig.getRegistryType(),
            springBootProviderConfig.getRegistryLoadBalanceType(),
            springBootProviderConfig.getReflectType(),
            springBootProviderConfig.getHeartbeatInterval(),
            springBootProviderConfig.getScanNotActiveChannelInterval());
    }
}
