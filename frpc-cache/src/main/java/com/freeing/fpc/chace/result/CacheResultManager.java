package com.freeing.fpc.chace.result;

import com.freeing.rpc.constants.RpcConstants;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 结果缓存管理器
 *
 * @author yanggy
 */
public class CacheResultManager<T> {
    /**
     * 缓存结果信息
     */
    private final Map<CacheResultKey, T> cacheResult = new ConcurrentHashMap<>();

    /**
     * 扫描结果缓存的线程池
     */
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    /**
     * 读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 读锁
     */
    private final Lock readLock = lock.readLock();

    /**
     * 写锁
     */
    private final Lock writeLock = lock.writeLock();

    /**
     * 结果缓存过期时长，单位毫秒
     */
    private int resultCacheExpire;

    public CacheResultManager(int resultCacheExpire, boolean enableResultCache){
        this.resultCacheExpire = resultCacheExpire;
        if (enableResultCache){
            this.startScanTask();
        }
    }

    private void startScanTask() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (cacheResult.size() > 0) {
                writeLock.lock();
                try {
                    Iterator<Map.Entry<CacheResultKey, T>> iterator = cacheResult.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<CacheResultKey, T> entry = iterator.next();
                        CacheResultKey cacheKey = entry.getKey();
                        // 当时间减去保存数据的缓存时间大于配置的时间间隔，则需要剔除缓存数据
                        if (System.currentTimeMillis() - cacheKey.getCacheTimeStamp() > resultCacheExpire){
                            cacheResult.remove(cacheKey);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }, 0, RpcConstants.RPC_SCAN_RESULT_CACHE_TIME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public T get(CacheResultKey key) {
        return cacheResult.get(key);
    }

    public void put(CacheResultKey key, T value) {
        writeLock.lock();
        try {
            cacheResult.put(key, value);
        }finally {
            writeLock.unlock();
        }
    }
}
