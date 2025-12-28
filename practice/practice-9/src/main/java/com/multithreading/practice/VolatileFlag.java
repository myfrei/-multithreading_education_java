package com.multithreading.practice;

/**
 * Класс с volatile флагом для демонстрации видимости изменений.
 */
public class VolatileFlag {
    private volatile boolean ready = false;
    private int value = 0;
    
    public void setReady(int val) {
        value = val;
        ready = true; // Запись в volatile гарантирует видимость value
    }
    
    public int waitForReady() throws InterruptedException {
        while (!ready) {
            Thread.yield();
        }
        return value;
    }
}

