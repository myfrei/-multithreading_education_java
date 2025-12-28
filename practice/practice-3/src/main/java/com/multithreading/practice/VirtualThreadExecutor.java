package com.multithreading.practice;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс для работы с виртуальными потоками (Java 21+).
 * 
 * Задание: Реализуйте метод executeWithVirtualThreads так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - Используйте Executors.newVirtualThreadPerTaskExecutor() для создания ExecutorService
 * - Виртуальные потоки идеальны для I/O-bound задач
 * - Не забывайте закрывать ExecutorService (try-with-resources)
 */
public class VirtualThreadExecutor {
    
    /**
     * Выполняет список задач в виртуальных потоках.
     * 
     * @param tasks список задач для выполнения
     * @return счетчик выполненных задач
     * @throws ExecutionException если произошла ошибка при выполнении
     * @throws InterruptedException если поток был прерван
     */
    public int executeWithVirtualThreads(List<Runnable> tasks) 
            throws ExecutionException, InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Void>> futures = new java.util.ArrayList<>();
            
            for (Runnable task : tasks) {
                futures.add(executor.submit(() -> {
                    task.run();
                    counter.incrementAndGet();
                    return null;
                }));
            }
            
            for (Future<Void> future : futures) {
                future.get();
            }
        }
        
        return counter.get();
    }
}

