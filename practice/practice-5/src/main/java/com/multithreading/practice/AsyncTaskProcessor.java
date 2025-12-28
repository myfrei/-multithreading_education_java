package com.multithreading.practice;

import java.util.concurrent.CompletableFuture;

/**
 * Класс для работы с асинхронными задачами через CompletableFuture.
 * 
 * Задание: Реализуйте методы так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - Используйте CompletableFuture.supplyAsync() для создания асинхронных задач
 * - thenApply() преобразует результат и возвращает новый CompletableFuture
 * - thenCompose() "разворачивает" вложенный CompletableFuture (аналог flatMap)
 * - thenCombine() комбинирует результаты двух независимых CompletableFuture
 * - allOf() ждет завершения всех задач
 * - exceptionally() обрабатывает только ошибки
 * - handle() обрабатывает и успех, и ошибку
 */
public class AsyncTaskProcessor {
    
    /**
     * Создает цепочку преобразований над асинхронным результатом.
     * 
     * @param initialValue начальное значение
     * @return CompletableFuture с преобразованным результатом
     */
    public CompletableFuture<String> createTransformationChain(String initialValue) {
        return CompletableFuture
            .supplyAsync(() -> initialValue)
            .thenApply(s -> s + " World")
            .thenApply(String::toUpperCase);
    }
    
    /**
     * Создает плоскую композицию двух асинхронных операций.
     * 
     * @param value начальное значение
     * @return CompletableFuture с результатом второй операции
     */
    public CompletableFuture<String> createComposition(int value) {
        return CompletableFuture
            .supplyAsync(() -> value)
            .thenCompose(v -> 
                CompletableFuture.supplyAsync(() -> "Result: " + v));
    }
    
    /**
     * Комбинирует результаты двух независимых асинхронных операций.
     * 
     * @param value1 первое значение
     * @param value2 второе значение
     * @return CompletableFuture с объединенным результатом
     */
    public CompletableFuture<String> combineResults(String value1, String value2) {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return value1;
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return value2;
        });
        
        return future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2);
    }
    
    /**
     * Ждет завершения всех переданных задач.
     * 
     * @param futures массив CompletableFuture для ожидания
     * @return CompletableFuture, который завершается когда все задачи завершены
     */
    @SafeVarargs
    public final CompletableFuture<Void> waitForAll(CompletableFuture<String>... futures) {
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Обрабатывает ошибку в асинхронной операции.
     * 
     * @param throwError если true, выбрасывает исключение
     * @return CompletableFuture с обработанным результатом
     */
    public CompletableFuture<String> handleError(boolean throwError) {
        return CompletableFuture
            .supplyAsync(() -> {
                if (throwError) {
                    throw new RuntimeException("Ошибка");
                }
                return "Успех";
            })
            .exceptionally(ex -> "Обработано: " + ex.getMessage());
    }
    
    /**
     * Обрабатывает и успех, и ошибку в асинхронной операции.
     * 
     * @param throwError если true, выбрасывает исключение
     * @return CompletableFuture с обработанным результатом
     */
    public CompletableFuture<String> handleSuccessOrError(boolean throwError) {
        return CompletableFuture
            .supplyAsync(() -> {
                if (throwError) {
                    throw new RuntimeException("Ошибка");
                }
                return "Успех";
            })
            .handle((result, ex) -> ex == null ? result.toUpperCase() : "Обработана ошибка");
    }
    
    /**
     * Обрабатывает результат асинхронной операции через thenAccept.
     * 
     * @param value значение для обработки
     * @param consumer потребитель результата
     * @return CompletableFuture, который завершается после обработки
     */
    public CompletableFuture<Void> processResult(int value, 
                                                 java.util.function.Consumer<Integer> consumer) {
        return CompletableFuture
            .supplyAsync(() -> value)
            .thenAccept(consumer);
    }
}

