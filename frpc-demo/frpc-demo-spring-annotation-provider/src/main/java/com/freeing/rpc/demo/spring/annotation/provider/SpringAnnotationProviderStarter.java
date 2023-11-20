package com.freeing.rpc.demo.spring.annotation.provider;

import com.freeing.rpc.demo.spring.annotation.provider.config.SpringAnnotationProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author yanggy
 */
public class SpringAnnotationProviderStarter {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringAnnotationProviderConfig.class);

    }
}
