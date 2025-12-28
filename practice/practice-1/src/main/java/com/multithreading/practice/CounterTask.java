package com.multithreading.practice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Задача, реализующая Runnable, которая увеличивает счетчик.
 */
public class CounterTask implements Runnable {
    private final AtomicInteger counter;
    
    public CounterTask(AtomicInteger counter) {
        this.counter = counter;
    }
    
    @Override
    public void run() {
        counter.incrementAndGet();
    }
}

