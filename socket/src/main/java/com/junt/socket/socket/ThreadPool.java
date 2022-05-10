package com.junt.socket.socket;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
public class ThreadPool {
    private static final String TAG = "ThreadPool";
    private static ThreadPool threadPoolInstance;

    private static final int DEFAULT_CORE_SIZE = Runtime.getRuntime().availableProcessors();
//    private static final int CORE_POOL_SIZE = DEFAULT_CORE_SIZE * 4;
//    private static final int MAX_POOL_SIZE = DEFAULT_CORE_SIZE * 5;
private static final int CORE_POOL_SIZE = DEFAULT_CORE_SIZE * 8;
    private static final int MAX_POOL_SIZE = DEFAULT_CORE_SIZE * 10;
    private ThreadPoolExecutor executor;

    private ThreadPool() {
        Log.i(TAG, "ThreadPool: " + DEFAULT_CORE_SIZE);
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                1,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE),
                Executors.defaultThreadFactory());
    }

    public static ThreadPool getInstance() {
        if (threadPoolInstance == null) {
            threadPoolInstance = new ThreadPool();
        }
        return threadPoolInstance;
    }

    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        executor.execute(runnable);
    }

    // 从线程队列中移除对象
    public void cancel(Runnable runnable) {
        if (executor != null) {
            executor.getQueue().remove(runnable);
        }
    }

}
