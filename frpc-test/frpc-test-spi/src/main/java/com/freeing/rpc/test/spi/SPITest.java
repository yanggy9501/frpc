package com.freeing.rpc.test.spi;

import com.freeing.rpc.spi.loader.ExtensionLoader;
import com.freeing.rpc.test.spi.service.SpiService;
import org.junit.Test;

/**
 * @author yanggy
 */
public class SPITest {

    @Test
    public void testSpiLoader(){
        SpiService spiService = ExtensionLoader.getExtension(SpiService.class, "spiService");
        String result = spiService.hello("rpc-spi");
        System.out.println(result);
    }
}
