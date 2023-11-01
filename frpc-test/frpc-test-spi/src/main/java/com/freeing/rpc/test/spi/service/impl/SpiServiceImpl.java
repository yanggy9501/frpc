package com.freeing.rpc.test.spi.service.impl;

import com.freeing.rpc.spi.annotation.SPIClass;
import com.freeing.rpc.test.spi.service.SpiService;

/**
 * @author yanggy
 */
@SPIClass
public class SpiServiceImpl implements SpiService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
