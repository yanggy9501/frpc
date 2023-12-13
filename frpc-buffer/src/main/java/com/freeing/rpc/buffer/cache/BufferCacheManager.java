package com.freeing.rpc.buffer.cache;

import com.freeing.rpc.common.exception.RpcException;
import com.freeing.rpc.constants.RpcConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 缓冲区实现
 *
 * @author yanggy
 */
public class BufferCacheManager<T> {

    private static volatile BufferCacheManager<?> instance;

    /**
     * 缓冲队列
     */
    private BlockingQueue<T> bufferQueue;

    public BufferCacheManager(int bufferSize) {
        if (bufferSize <= 0) {
            bufferSize = RpcConstants.DEFAULT_BUFFER_SIZE;
        }
        bufferQueue = new ArrayBlockingQueue<>(bufferSize);
    }

    public static <T> BufferCacheManager<T> getInstance(int bufferSize){
        if (instance == null){
            synchronized (BufferCacheManager.class){
                if (instance == null){
                    instance = new BufferCacheManager<T>(bufferSize);
                }
            }
        }
        return (BufferCacheManager<T>) instance;
    }

    public void put(T t) {
        try {
            bufferQueue.put(t);
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }

    public T take(){
        try {
            return bufferQueue.take();
        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }
}
