package com.freeing.rpc.consumer.common.future;

import com.freeing.rpc.protocol.RpcProtocol;
import com.freeing.rpc.protocol.request.RpcRequest;
import com.freeing.rpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * RPC框架获取异步结果的自定义Future
 *
 * @author yanggy
 */
public class RPCFuture extends CompletableFuture<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;

    private RpcProtocol<RpcRequest> requestRpcProtocol;

    private RpcProtocol<RpcResponse> responseRpcProtocol;

    private long startTime;

    private long responseTimeThreshold = 5000;

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() {
        sync.acquire(-1);
        if (Objects.nonNull(this.responseRpcProtocol)) {
            return this.responseRpcProtocol.getBody().getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (Objects.nonNull(this.responseRpcProtocol)) {
                return this .responseRpcProtocol.getBody().getResult();
            }
            return null;
        }
        throw new RuntimeException("Timeout exception. Request id: " + this.requestRpcProtocol.getHeader().getRequestId()
            + ". Request class name: " + this.requestRpcProtocol.getBody().getClassName()
            + ". Request method: " + this.requestRpcProtocol.getBody().getMethodName());

    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too slow. Request id = " + responseRpcProtocol.getHeader().getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        /** future status */
        private final int done = 1;
        private final int pending = 0;

        /**
         * 获取一个资源，如果当前资源为 1 则可以获取
         */
        @Override
        protected boolean tryAcquire(int acquires) {
            return getState() == done;
        }

        /**
         * 释放一个资源
         */
        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                return compareAndSetState(pending, done);
            }
            return false;
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}
