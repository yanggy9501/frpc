package com.freeing.rpc.fusing.counter;

import com.freeing.rpc.constants.RpcConstants;
import com.freeing.rpc.fusing.AbstractFusingInvoker;
import com.freeing.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在一段时间内基于错误数量的熔断策略
 *
 * @author yanggy
 */
@SPIClass
public class CounterFusingInvoker extends AbstractFusingInvoker {
    private static final Logger logger = LoggerFactory.getLogger(CounterFusingInvoker.class);

    @Override
    public boolean invokeFusingStrategy() {
        logger.info("execute counter fusing strategy, current fusing status is {}", fusingStatus.get());
        switch (fusingStatus.get()){
            // 关闭状态
            case RpcConstants.FUSING_STATUS_CLOSED:
                return this.invokeClosedFusingStrategy();
            // 半开启状态
            case RpcConstants.FUSING_STATUS_HALF_OPEN:
                return this.invokeHalfOpenFusingStrategy();
            // 开启状态
            case RpcConstants.FUSING_STATUS_OPEN:
                return this.invokeOpenFusingStrategy();
            default:
                return this.invokeClosedFusingStrategy();
        }
    }

    private boolean invokeOpenFusingStrategy() {
        // 获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        // 超过一个指定的时间范围，则将状态设置为半开启状态
        if (currentTimeStamp - lastTimeStamp >= milliSeconds){
            fusingStatus.set(RpcConstants.FUSING_STATUS_HALF_OPEN);
            lastTimeStamp = currentTimeStamp;
            this.resetCount();
            return false;
        }
        return true;
    }

    private boolean invokeHalfOpenFusingStrategy() {
        // 获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        // 服务已经恢复
        if (currentFailureCounter.get() <= 0) {
            fusingStatus.set(RpcConstants.FUSING_STATUS_CLOSED);
            lastTimeStamp = currentTimeStamp;
            this.resetCount();
            return false;
        }
        // 服务未恢复
        fusingStatus.set(RpcConstants.FUSING_STATUS_OPEN);
        lastTimeStamp = currentTimeStamp;
        return true;
    }

    private boolean invokeClosedFusingStrategy() {
        // 获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        // 超过一个指定的时间范围
        if (currentTimeStamp - lastTimeStamp >= milliSeconds){
            lastTimeStamp = currentTimeStamp;
            this.resetCount();
            return false;
        }
        // 超出配置的错误数量
        if (currentFailureCounter.get() >= totalFailure){
            lastTimeStamp = currentTimeStamp;
            fusingStatus.set(RpcConstants.FUSING_STATUS_OPEN);
            return true;
        }
        return false;
    }
}
