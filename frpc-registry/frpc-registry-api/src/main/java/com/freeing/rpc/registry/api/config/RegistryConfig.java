package com.freeing.rpc.registry.api.config;

import java.io.Serializable;

/**
 * 注册配置类
 *
 * @author yanggy
 */
public class RegistryConfig implements Serializable {
    private static final long serialVersionUID = -7248658103788758893L;

    /**
     * 注册地址
     */
    private String registrAddress;

    /**
     * 注册类型
     */
    private String registryType;

    public RegistryConfig(String registrAddress, String registryType) {
        this.registrAddress = registrAddress;
        this.registryType = registryType;
    }

    public String getRegistrAddress() {
        return registrAddress;
    }

    public void setRegistrAddress(String registrAddress) {
        this.registrAddress = registrAddress;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }
}
