package com.multithreading.practice;

/**
 * Потокобезопасный счетчик с использованием synchronized.
 */
public class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}

