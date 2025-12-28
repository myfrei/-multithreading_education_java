package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Используйте volatile для обеспечения видимости изменений между потоками
 * 2. Реализуйте правильную синхронизацию для гарантии happens-before
 * 3. Используйте synchronized для обеспечения видимости и атомарности
 * 4. Правильно реализуйте Double-Checked Locking с volatile
 */
class MemoryModelTest {

    @Test
    @Timeout(10)
    void testVolatileVisibility() throws InterruptedException {
        VolatileFlag flag = new VolatileFlag();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.submit(() -> {
            try {
                Thread.sleep(100);
                flag.setReady(42);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        int result = flag.waitForReady();
        executor.shutdown();
        
        assertEquals(42, result,
            "Значение должно быть видно благодаря volatile");
    }

    @Test
    @Timeout(10)
    void testSynchronizedHappensBefore() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                counter.increment();
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(100, counter.getCount(),
            "Synchronized должен гарантировать видимость изменений");
    }

    @Test
    @Timeout(10)
    void testDoubleCheckedLocking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        Set<Singleton> instances = ConcurrentHashMap.newKeySet();
        
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                instances.add(Singleton.getInstance());
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(1, instances.size(),
            "Должен быть создан только один экземпляр");
    }

    @Test
    @Timeout(10)
    void testThreadStartHappensBefore() throws InterruptedException {
        DataHolder holder = new DataHolder();
        holder.setValue(42);
        
        Thread thread = new Thread(() -> {
            // Поток видит значение, установленное до start()
            assertEquals(42, holder.getValue(),
                "Значение должно быть видно благодаря happens-before start()");
        });
        
        thread.start();
        thread.join();
    }

    @Test
    @Timeout(10)
    void testThreadJoinHappensBefore() throws InterruptedException {
        ResultHolder holder = new ResultHolder();
        
        Thread thread = new Thread(() -> {
            holder.setResult(100);
        });
        
        thread.start();
        thread.join(); // join() гарантирует happens-before
        
        assertEquals(100, holder.getResult(),
            "Значение должно быть видно благодаря happens-before join()");
    }

    @Test
    @Timeout(10)
    void testVolatileVsSynchronized() throws InterruptedException {
        VolatileCounter counter = new VolatileCounter();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                counter.increment(); // Может быть потеряно из-за неатомарности
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // volatile не гарантирует атомарность, поэтому результат может быть меньше 100
        // Но для демонстрации happens-before это нормально
        assertTrue(counter.getCount() > 0,
            "Volatile обеспечивает видимость, но не атомарность");
    }
}

