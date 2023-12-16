package com.freeing.rpc.ratelimiter;

import com.freeing.rpc.ratelimiter.api.RateLimiterInvoker;

/**
 * 抽象限流器
 *
 * @author yanggy
 */
public abstract class AbstractRateLimiterInvoker implements RateLimiterInvoker {

    protected int permits;

    protected int milliSeconds;

    @Override
    public void init(int permits, int milliSeconds) {
        this.permits = permits;
        this.milliSeconds = milliSeconds;
    }
}
