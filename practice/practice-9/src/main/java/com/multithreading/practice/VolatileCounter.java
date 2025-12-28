package com.multithreading.practice;

/**
 * Счетчик с volatile для демонстрации разницы между volatile и synchronized.
 * ВАЖНО: volatile не гарантирует атомарность операции increment!
 */
public class VolatileCounter {
    private volatile int count = 0;
    
    public void increment() {
        count++; // НЕ атомарно, даже с volatile!
    }
    
    public int getCount() {
        return count;
    }
}

