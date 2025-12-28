package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Создайте класс CounterTask, реализующий Runnable, который увеличивает счетчик
 * 2. Создайте класс SumTask, реализующий Callable<Integer>, который суммирует числа от 1 до n
 * 3. Реализуйте метод createThreadWithRunnable, который создает и запускает поток с Runnable
 * 4. Реализуйте метод executeCallable, который выполняет Callable через ExecutorService
 */
class ThreadTaskTest {

    @Test
    @Timeout(5)
    void testRunnableTask() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        
        CounterTask task = new CounterTask(counter);
        Thread thread = new Thread(task);
        thread.start();
        thread.join();
        
        assertEquals(1, counter.get(), "Счетчик должен быть увеличен на 1");
    }

    @Test
    @Timeout(5)
    void testCallableTask() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        SumTask task = new SumTask(10);
        Future<Integer> future = executor.submit(task);
        
        int result = future.get();
        assertEquals(55, result, "Сумма чисел от 1 до 10 должна быть 55");
        
        executor.shutdown();
    }

    @Test
    @Timeout(5)
    void testMultipleThreads() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        CounterTask task = new CounterTask(counter);
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(threadCount, counter.get(), 
            "Счетчик должен быть равен количеству потоков");
    }

    @Test
    @Timeout(5)
    void testThreadInterruption() throws InterruptedException {
        AtomicInteger workDone = new AtomicInteger(0);
        
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                    workDone.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        thread.start();
        Thread.sleep(250);
        thread.interrupt();
        thread.join();
        
        assertTrue(workDone.get() >= 2, 
            "Поток должен выполнить работу до прерывания");
        assertTrue(thread.isInterrupted() || !thread.isAlive(), 
            "Поток должен быть прерван или завершен");
    }

    @Test
    @Timeout(5)
    void testCallableWithException() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<Integer> future = executor.submit(() -> {
            throw new IllegalArgumentException("Тестовая ошибка");
        });
        
        assertThrows(ExecutionException.class, () -> future.get(),
            "Должно быть выброшено ExecutionException");
        
        executor.shutdown();
    }
}

