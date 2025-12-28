package com.multithreading.practice;

import java.util.List;
import java.util.concurrent.*;

/**
 * Класс для обработки задач через ExecutorService.
 * 
 * Задание: Реализуйте методы так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - Используйте Executors.newFixedThreadPool() для создания пула потоков
 * - Не забывайте вызывать shutdown() и awaitTermination()
 * - Правильно обрабатывайте исключения при получении результатов Future
 */
public class TaskProcessor {
    private final ExecutorService executor;
    
    public TaskProcessor(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    /**
     * Обрабатывает задачу и возвращает Future с результатом.
     * 
     * @param task задача для выполнения
     * @return Future с результатом выполнения задачи
     */
    public <T> Future<T> processTask(Callable<T> task) {
        return executor.submit(task);
    }
    
    /**
     * Обрабатывает список задач параллельно и возвращает список Future.
     * 
     * @param tasks список задач для выполнения
     * @return список Future с результатами
     */
    public <T> List<Future<T>> processTasks(List<Callable<T>> tasks) {
        List<Future<T>> futures = new java.util.ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(executor.submit(task));
        }
        return futures;
    }
    
    /**
     * Корректно завершает ExecutorService, дожидаясь завершения всех задач.
     * 
     * @param timeout максимальное время ожидания
     * @param unit единица времени
     * @return true, если все задачи завершились в указанное время
     * @throws InterruptedException если поток был прерван
     */
    public boolean shutdownGracefully(long timeout, TimeUnit unit) throws InterruptedException {
        executor.shutdown();
        return executor.awaitTermination(timeout, unit);
    }
    
    /**
     * Проверяет, завершен ли ExecutorService.
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }
}

