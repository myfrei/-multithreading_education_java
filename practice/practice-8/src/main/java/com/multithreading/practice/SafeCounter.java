package com.multithreading.practice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потокобезопасный счетчик без состояния гонки.
 */
public class SafeCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int get() {
        return count.get();
    }
}

