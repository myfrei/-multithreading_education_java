package com.multithreading.practice;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Класс для параллельной обработки данных.
 * 
 * Задание: Реализуйте метод processInParallel так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - Используйте ExecutorService для параллельного выполнения
 * - Преобразуйте каждый элемент входного списка в задачу
 * - Соберите результаты из Future
 */
public class ParallelProcessor {
    
    /**
     * Обрабатывает список элементов параллельно, применяя функцию к каждому элементу.
     * 
     * @param input входной список элементов
     * @param processor функция для обработки каждого элемента
     * @param threadPoolSize размер пула потоков
     * @return список обработанных элементов
     * @throws InterruptedException если поток был прерван
     */
    public <T, R> List<R> processInParallel(List<T> input, 
                                           java.util.function.Function<T, R> processor,
                                           int threadPoolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            List<Future<R>> futures = input.stream()
                .map(item -> executor.submit(() -> processor.apply(item)))
                .collect(Collectors.toList());
            
            return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }
}

