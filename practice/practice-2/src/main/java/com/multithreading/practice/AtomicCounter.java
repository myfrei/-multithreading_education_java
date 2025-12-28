package com.multithreading.practice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потокобезопасный счетчик с использованием AtomicInteger.
 */
public class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
    
    public int getCount() {
        return count.get();
    }
}

