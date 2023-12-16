package com.freeing.rpc.ratelimiter.api;

import com.freeing.rpc.spi.annotation.SPI;

/**
 * 限流调用器SPI，秒级单位限流
 */
@SPI
public interface RateLimiterInvoker {

    /**
     * 限流方法
     *
     * @return
     */
    boolean tryAcquire();

    /**
     * 释放资源
     */
    void release();

    /**
     * 在milliSeconds毫秒内最多允许通过permits个请求
     * @param permits 在milliSeconds毫秒内最多能够通过的请求个数
     * @param milliSeconds 毫秒数
     */
    default void init(int permits, int milliSeconds) {

    }
}
