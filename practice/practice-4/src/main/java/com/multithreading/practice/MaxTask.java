package com.multithreading.practice;

import java.util.concurrent.RecursiveTask;

/**
 * Задача для поиска максимального элемента в массиве с использованием ForkJoinPool.
 */
public class MaxTask extends RecursiveTask<Integer> {
    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 100;
    
    public MaxTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Integer compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            int max = array[start];
            for (int i = start + 1; i < end; i++) {
                if (array[i] > max) {
                    max = array[i];
                }
            }
            return max;
        } else {
            int mid = start + length / 2;
            MaxTask left = new MaxTask(array, start, mid);
            MaxTask right = new MaxTask(array, mid, end);
            left.fork();
            int rightResult = right.compute();
            int leftResult = left.join();
            return Math.max(leftResult, rightResult);
        }
    }
}

