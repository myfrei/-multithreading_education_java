package com.multithreading.practice;

/**
 * Класс для демонстрации happens-before через Thread.start().
 */
public class DataHolder {
    private int value = 0;
    
    public void setValue(int val) {
        value = val;
    }
    
    public int getValue() {
        return value;
    }
}

