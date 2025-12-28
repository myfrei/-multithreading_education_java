package com.multithreading.practice;

/**
 * Класс для демонстрации happens-before через Thread.join().
 */
public class ResultHolder {
    private int result = 0;
    
    public void setResult(int val) {
        result = val;
    }
    
    public int getResult() {
        return result;
    }
}

