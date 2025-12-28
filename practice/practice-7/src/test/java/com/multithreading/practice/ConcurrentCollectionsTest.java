package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Используйте CopyOnWriteArrayList для сценариев "много читателей, мало писателей"
 * 2. Реализуйте ConcurrentCache для потокобезопасного кэша
 * 3. Реализуйте ProducerConsumer для паттерна "Производитель-Потребитель"
 * 4. Правильно используйте атомарные операции в ConcurrentHashMap
 * 
 * Подсказки:
 * - CopyOnWriteArrayList создает копию при записи, идеален для частых чтений
 * - ConcurrentHashMap использует тонкую блокировку на уровне сегментов
 * - BlockingQueue.put() блокируется, если очередь полна, take() блокируется, если очередь пуста
 * - Используйте computeIfAbsent, compute, merge для атомарных операций в ConcurrentHashMap
 */
class ConcurrentCollectionsTest {

    /**
     * Тест проверяет работу CopyOnWriteArrayList в многопоточной среде.
     * Список должен безопасно обрабатывать множество читателей и несколько писателей.
     */
    @Test
    @Timeout(10)
    void testCopyOnWriteArrayList() throws InterruptedException {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        
        // Писатели (редко)
        for (int i = 0; i < 5; i++) {
            final int id = i;
            executor.submit(() -> {
                list.add("Item" + id);
                latch.countDown();
            });
        }
        
        // Читатели (часто)
        for (int i = 0; i < 95; i++) {
            executor.submit(() -> {
                list.size(); // Чтение
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertTrue(list.size() >= 5, 
            "Список должен содержать добавленные элементы");
    }

    /**
     * Тест проверяет работу ConcurrentCache в многопоточной среде.
     * Кэш должен безопасно обрабатывать одновременные операции записи.
     */
    @Test
    @Timeout(10)
    void testConcurrentHashMap() throws InterruptedException {
        ConcurrentCache cache = new ConcurrentCache();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        
        for (int i = 0; i < 100; i++) {
            final int id = i;
            executor.submit(() -> {
                cache.put("key" + id, id);
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(100, cache.size(),
            "Кэш должен содержать все добавленные элементы");
    }

    /**
     * Тест проверяет атомарные операции в ConcurrentCache.
     * Метод incrementCounter должен атомарно увеличивать счетчик.
     */
    @Test
    @Timeout(10)
    void testConcurrentHashMapAtomicOperations() {
        ConcurrentCache cache = new ConcurrentCache();
        
        cache.incrementCounter("counter1");
        cache.incrementCounter("counter1");
        cache.incrementCounter("counter2");
        
        AtomicInteger counter1 = cache.getCounter("counter1");
        AtomicInteger counter2 = cache.getCounter("counter2");
        
        assertEquals(2, counter1.get(),
            "Счетчик должен быть увеличен дважды");
        assertEquals(1, counter2.get(),
            "Второй счетчик должен быть увеличен один раз");
    }

    /**
     * Тест проверяет паттерн "Производитель-Потребитель" с BlockingQueue.
     * ProducerConsumer должен корректно производить и потреблять элементы.
     */
    @Test
    @Timeout(10)
    void testBlockingQueueProducerConsumer() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        ProducerConsumer producerConsumer = new ProducerConsumer(queue);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1);
        
        // Производитель
        executor.submit(() -> {
            try {
                producerConsumer.produce(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Потребитель
        executor.submit(() -> {
            try {
                producerConsumer.consume(20);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertEquals(20, producerConsumer.getProducedCount(),
            "Должно быть произведено 20 элементов");
        assertEquals(20, producerConsumer.getConsumedCount(),
            "Должно быть потреблено 20 элементов");
    }

    @Test
    @Timeout(10)
    void testBlockingQueueTimeout() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
        
        // Заполняем очередь
        for (int i = 0; i < 5; i++) {
            queue.put("item" + i);
        }
        
        // Попытка добавить в полную очередь с тайм-аутом
        boolean result = queue.offer("item6", 100, TimeUnit.MILLISECONDS);
        
        assertFalse(result,
            "Добавление должно вернуть false при тайм-ауте");
    }

    /**
     * Тест проверяет атомарное обновление через compute в ConcurrentCache.
     * Метод computeValue должен атомарно обновлять значение.
     */
    @Test
    @Timeout(10)
    void testConcurrentHashMapCompute() {
        ConcurrentCache cache = new ConcurrentCache();
        
        cache.computeValue("key", (k, v) -> v == null ? 1 : v + 1);
        cache.computeValue("key", (k, v) -> v == null ? 1 : v + 1);
        
        assertEquals(2, cache.get("key"),
            "Значение должно быть увеличено дважды");
    }

    /**
     * Тест проверяет объединение значений через merge в ConcurrentCache.
     * Метод mergeValue должен атомарно объединять значения.
     */
    @Test
    @Timeout(10)
    void testConcurrentHashMapMerge() {
        ConcurrentCache cache = new ConcurrentCache();
        
        cache.mergeValue("key", 1, Integer::sum);
        cache.mergeValue("key", 2, Integer::sum);
        cache.mergeValue("key", 3, Integer::sum);
        
        assertEquals(6, cache.get("key"),
            "Значения должны быть объединены");
    }
}

