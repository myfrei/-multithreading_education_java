package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Реализуйте SumTask, который суммирует элементы массива рекурсивно
 * 2. Реализуйте MaxTask, который находит максимальный элемент в массиве
 * 3. Используйте правильный порог (threshold) для разделения задач
 * 4. Правильно используйте fork() и join()
 */
class ForkJoinPoolTest {

    /**
     * Тест проверяет суммирование элементов массива через SumTask.
     * Задача должна рекурсивно разделяться на подзадачи до достижения порога.
     */
    @Test
    @Timeout(10)
    void testSumTask() {
        long[] array = new long[1000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1;
        }
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        long expectedSum = array.length * (array.length + 1) / 2;
        
        SumTask task = new SumTask(array, 0, array.length);
        long result = pool.invoke(task);
        
        assertEquals(expectedSum, result,
            "Сумма должна быть вычислена корректно");
    }

    /**
     * Тест проверяет поиск максимального элемента в массиве через MaxTask.
     * Задача должна использовать алгоритм "разделяй и властвуй" для параллельного поиска максимума.
     */
    @Test
    @Timeout(10)
    void testMaxTask() {
        int[] array = new int[1000];
        int maxValue = 0;
        for (int i = 0; i < array.length; i++) {
            array[i] = (int) (Math.random() * 10000);
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        
        MaxTask task = new MaxTask(array, 0, array.length);
        int result = pool.invoke(task);
        
        assertEquals(maxValue, result,
            "Максимальный элемент должен быть найден корректно");
    }

    @Test
    @Timeout(10)
    void testWorkStealing() throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(4);
        
        class SimpleTask extends RecursiveTask<Integer> {
            private final int value;
            
            SimpleTask(int value) {
                this.value = value;
            }
            
            @Override
            protected Integer compute() {
                if (value < 10) {
                    return value;
                } else {
                    SimpleTask left = new SimpleTask(value / 2);
                    SimpleTask right = new SimpleTask(value - value / 2);
                    left.fork();
                    int rightResult = right.compute();
                    int leftResult = left.join();
                    return leftResult + rightResult;
                }
            }
        }
        
        SimpleTask task = new SimpleTask(100);
        int result = pool.invoke(task);
        
        assertEquals(100, result,
            "Work-stealing должен обеспечить корректное выполнение");
        
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}

