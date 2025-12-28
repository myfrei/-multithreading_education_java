package com.multithreading.practice;

import java.util.concurrent.Callable;

/**
 * Задача, реализующая Callable, которая суммирует числа от 1 до n.
 */
public class SumTask implements Callable<Integer> {
    private final int n;
    
    public SumTask(int n) {
        this.n = n;
    }
    
    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }
}

