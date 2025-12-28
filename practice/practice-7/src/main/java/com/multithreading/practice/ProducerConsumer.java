package com.multithreading.practice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация паттерна "Производитель-Потребитель" с использованием BlockingQueue.
 * 
 * Задание: Реализуйте методы так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - BlockingQueue.put() блокируется, если очередь полна
 * - BlockingQueue.take() блокируется, если очередь пуста
 * - BlockingQueue.offer() с тайм-аутом возвращает false, если не удалось добавить
 */
public class ProducerConsumer {
    private final BlockingQueue<Integer> queue;
    private final AtomicInteger produced = new AtomicInteger(0);
    private final AtomicInteger consumed = new AtomicInteger(0);
    
    public ProducerConsumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
    
    /**
     * Производит элементы и помещает их в очередь.
     * 
     * @param count количество элементов для производства
     * @throws InterruptedException если поток был прерван
     */
    public void produce(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            queue.put(i);
            produced.incrementAndGet();
        }
    }
    
    /**
     * Потребляет элементы из очереди.
     * 
     * @param count количество элементов для потребления
     * @throws InterruptedException если поток был прерван
     */
    public void consume(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            queue.take();
            consumed.incrementAndGet();
        }
    }
    
    /**
     * Возвращает количество произведенных элементов.
     * 
     * @return количество произведенных элементов
     */
    public int getProducedCount() {
        return produced.get();
    }
    
    /**
     * Возвращает количество потребленных элементов.
     * 
     * @return количество потребленных элементов
     */
    public int getConsumedCount() {
        return consumed.get();
    }
}

