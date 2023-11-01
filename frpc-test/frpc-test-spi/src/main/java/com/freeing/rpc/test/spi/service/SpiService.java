package com.freeing.rpc.test.spi.service;

import com.freeing.rpc.spi.annotation.SPI;

/**
 * @author yanggy
 */
@SPI("spiService")
public interface SpiService {
    String hello(String name);
}
