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
 * 1. Исправьте состояние гонки в небезопасном счетчике
 * 2. Избегайте дедлока при захвате нескольких ресурсов
 * 3. Реализуйте потокобезопасный счетчик без состояния гонки
 * 4. Используйте правильный порядок захвата ресурсов для предотвращения дедлока
 */
class DeadlockRaceConditionTest {

    /**
     * Тест проверяет исправление состояния гонки.
     * SafeCounter должен использовать правильную синхронизацию для предотвращения состояния гонки.
     */
    @Test
    @Timeout(10)
    void testRaceConditionFixed() throws InterruptedException {
        SafeCounter counter = new SafeCounter();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount * incrementsPerThread, counter.get(),
            "Счетчик должен быть равен количеству потоков * инкрементов");
    }

    /**
     * Тест проверяет предотвращение дедлока через одинаковый порядок захвата ресурсов.
     * Оба потока должны захватывать замки в одинаковом порядке.
     */
    @Test
    @Timeout(10)
    void testDeadlockPrevention() throws InterruptedException {
        Object lock1 = new Object();
        Object lock2 = new Object();
        AtomicInteger completed = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        // Поток 1: захватывает lock1, затем lock2
        executor.submit(() -> {
            synchronized (lock1) {
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    completed.incrementAndGet();
                }
            }
            latch.countDown();
        });
        
        // Поток 2: также захватывает lock1, затем lock2 (одинаковый порядок!)
        executor.submit(() -> {
            synchronized (lock1) {
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    completed.incrementAndGet();
                }
            }
            latch.countDown();
        });
        
        boolean finished = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertTrue(finished, "Потоки должны завершиться без дедлока");
        assertEquals(2, completed.get(),
            "Оба потока должны завершиться");
    }

    @Test
    @Timeout(10)
    void testSafeCounter() throws InterruptedException {
        SafeCounter counter = new SafeCounter();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount * incrementsPerThread, counter.get(),
            "Безопасный счетчик должен работать корректно");
    }

    /**
     * Тест проверяет использование ReentrantLock с тайм-аутом для предотвращения дедлока.
     * Потоки должны использовать tryLock(timeout) для избежания бесконечного ожидания.
     */
    @Test
    @Timeout(10)
    void testReentrantLockTimeout() throws InterruptedException {
        java.util.concurrent.locks.ReentrantLock lock1 = new java.util.concurrent.locks.ReentrantLock();
        java.util.concurrent.locks.ReentrantLock lock2 = new java.util.concurrent.locks.ReentrantLock();
        AtomicInteger completed = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        executor.submit(() -> {
            try {
                if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                completed.incrementAndGet();
                            } finally {
                                lock2.unlock();
                            }
                        }
                    } finally {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });
        
        executor.submit(() -> {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                completed.incrementAndGet();
                            } finally {
                                lock1.unlock();
                            }
                        }
                    } finally {
                        lock2.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });
        
        boolean finished = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertTrue(finished, "Потоки должны завершиться благодаря тайм-аутам");
        assertTrue(completed.get() >= 1,
            "Хотя бы один поток должен завершиться");
    }

    /**
     * Тест проверяет использование атомарных операций для предотвращения состояния гонки.
     * Все операции должны быть атомарными.
     */
    @Test
    @Timeout(10)
    void testAtomicOperations() throws InterruptedException {
        SafeCounter counter = new SafeCounter();
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount * 100, counter.get(),
            "Атомарные операции должны работать корректно");
    }
}

