package com.multithreading.practice;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Потокобезопасный счетчик с использованием ReentrantLock.
 */
public class LockCounter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
    
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

