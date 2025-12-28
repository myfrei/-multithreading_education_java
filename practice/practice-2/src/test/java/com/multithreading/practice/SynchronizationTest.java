package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Реализуйте потокобезопасный счетчик с использованием synchronized
 * 2. Реализуйте потокобезопасный счетчик с использованием AtomicInteger
 * 3. Реализуйте счетчик с использованием ReentrantLock
 * 4. Реализуйте кэш с использованием ReadWriteLock
 * 5. Реализуйте пул ресурсов с использованием Semaphore
 */
class SynchronizationTest {

    @Test
    @Timeout(10)
    void testSynchronizedCounter() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertEquals(threadCount * incrementsPerThread, counter.getCount(),
            "Счетчик должен быть равен количеству потоков * инкрементов");
    }

    @Test
    @Timeout(10)
    void testAtomicCounter() throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertEquals(threadCount * incrementsPerThread, counter.getCount(),
            "Атомарный счетчик должен работать корректно");
    }

    @Test
    @Timeout(10)
    void testReentrantLockCounter() throws InterruptedException {
        LockCounter counter = new LockCounter();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertEquals(threadCount * incrementsPerThread, counter.getCount(),
            "Счетчик с ReentrantLock должен работать корректно");
    }

    @Test
    @Timeout(10)
    void testReadWriteLockCache() throws InterruptedException {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // Писатели
        for (int i = 0; i < 5; i++) {
            final int id = i;
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.put("key" + id + "_" + j, "value" + id + "_" + j);
                }
            });
        }
        
        // Читатели
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.get("key0_" + j);
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertNotNull(cache.get("key0_0"), "Кэш должен содержать записанные значения");
    }

    @Test
    @Timeout(10)
    void testSemaphoreResourcePool() throws InterruptedException {
        ResourcePool pool = new ResourcePool(3);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    pool.acquire();
                    Thread.sleep(100);
                    assertTrue(pool.getActiveCount() <= 3, 
                        "Активных ресурсов не должно быть больше 3");
                    pool.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(0, pool.getActiveCount(), 
            "Все ресурсы должны быть освобождены");
    }

    @Test
    @Timeout(10)
    void testCountDownLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger completedTasks = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(5, completedTasks.get(), 
            "Все задачи должны быть выполнены");
    }
}

