package com.freeing.rpc.ratelimiter.counter;

import com.freeing.rpc.ratelimiter.AbstractRateLimiterInvoker;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器限流
 *
 * @author yanggy
 */
@SPIClass
public class CounterRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private static final Logger logger = LoggerFactory.getLogger(CounterRateLimiterInvoker.class);

    private final AtomicInteger currentCount = new AtomicInteger(0);
    private volatile long lastTimeStamp = System.currentTimeMillis();

    @Override
    public boolean tryAcquire() {
        logger.info("execute counter rate limiter...");
        // 获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        // 超过一个执行周期，重新计数
        if (currentTimeStamp - lastTimeStamp >= milliSeconds) {
            lastTimeStamp = currentTimeStamp;
            currentCount.set(0);
            return true;
        }
        // 当请求数小于配置的数量
        if (currentCount.incrementAndGet() <= permits) {
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        try {
            currentCount.decrementAndGet();
        } finally {

        }
    }
}
