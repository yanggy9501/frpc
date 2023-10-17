package com.freeing.rpc.common.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务提供者端线程池
 *
 * @author yanggy
 */
public class ServerThreadPool {

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR;

    static {
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            16,
            16,
            600L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(65536)
        );
    }

    public static void submit(Runnable taks) {
        THREAD_POOL_EXECUTOR.submit(taks);
    }

    public static void shutdown() {
        THREAD_POOL_EXECUTOR.shutdown();
    }
}
