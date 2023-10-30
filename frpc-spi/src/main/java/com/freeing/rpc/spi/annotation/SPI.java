package com.freeing.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * @SPI
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {
    /**
     * 默认的实现方式
     */
    String value() default "";
}
