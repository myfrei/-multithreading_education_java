package com.multithreading.practice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Пул ресурсов с использованием Semaphore.
 */
public class ResourcePool {
    private final Semaphore semaphore;
    private final AtomicInteger activeResources = new AtomicInteger(0);
    
    public ResourcePool(int poolSize) {
        this.semaphore = new Semaphore(poolSize);
    }
    
    public void acquire() throws InterruptedException {
        semaphore.acquire();
        activeResources.incrementAndGet();
    }
    
    public void release() {
        activeResources.decrementAndGet();
        semaphore.release();
    }
    
    public int getActiveCount() {
        return activeResources.get();
    }
}

