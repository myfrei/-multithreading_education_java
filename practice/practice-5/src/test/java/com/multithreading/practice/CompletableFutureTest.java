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
 * 1. Реализуйте цепочки операций с использованием thenApply, thenCompose в AsyncTaskProcessor
 * 2. Реализуйте комбинирование нескольких CompletableFuture через thenCombine, allOf
 * 3. Реализуйте обработку ошибок через exceptionally, handle
 * 4. Реализуйте тайм-ауты для асинхронных операций
 * 
 * Подсказки:
 * - thenApply() преобразует результат и возвращает новый CompletableFuture
 * - thenCompose() "разворачивает" вложенный CompletableFuture (аналог flatMap)
 * - thenCombine() комбинирует результаты двух независимых CompletableFuture
 * - allOf() ждет завершения всех задач
 * - exceptionally() обрабатывает только ошибки, handle() обрабатывает и успех, и ошибку
 */
class CompletableFutureTest {

    /**
     * Тест проверяет цепочку преобразований через thenApply.
     * AsyncTaskProcessor должен создать цепочку преобразований.
     */
    @Test
    @Timeout(10)
    void testThenApply() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        CompletableFuture<String> future = processor.createTransformationChain("Hello");
        
        assertEquals("HELLO WORLD", future.get(),
            "Цепочка преобразований должна работать корректно");
    }

    /**
     * Тест проверяет плоскую композицию через thenCompose.
     * AsyncTaskProcessor должен создать композицию двух асинхронных операций.
     */
    @Test
    @Timeout(10)
    void testThenCompose() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        CompletableFuture<String> future = processor.createComposition(10);
        
        assertEquals("Result: 10", future.get(),
            "thenCompose должен разворачивать вложенный CompletableFuture");
    }

    /**
     * Тест проверяет комбинирование двух независимых CompletableFuture через thenCombine.
     * AsyncTaskProcessor должен объединить результаты двух асинхронных операций.
     */
    @Test
    @Timeout(10)
    void testThenCombine() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        CompletableFuture<String> combined = processor.combineResults("Hello", "World");
        
        assertEquals("Hello World", combined.get(),
            "Результаты должны быть объединены");
    }

    /**
     * Тест проверяет ожидание завершения всех задач через allOf.
     * AsyncTaskProcessor должен дождаться завершения всех задач.
     */
    @Test
    @Timeout(10)
    void testAllOf() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Task1");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Task2");
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "Task3");
        
        CompletableFuture<Void> allOf = processor.waitForAll(future1, future2, future3);
        allOf.get();
        
        assertTrue(future1.isDone() && future2.isDone() && future3.isDone(),
            "Все задачи должны быть завершены");
    }

    /**
     * Тест проверяет обработку ошибок через exceptionally.
     * AsyncTaskProcessor должен обработать ошибку и вернуть альтернативное значение.
     */
    @Test
    @Timeout(10)
    void testExceptionally() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        CompletableFuture<String> future = processor.handleError(true);
        
        String result = future.get();
        assertTrue(result.contains("Обработано"),
            "Ошибка должна быть обработана");
    }

    /**
     * Тест проверяет обработку и успеха, и ошибки через handle.
     * AsyncTaskProcessor должен обработать оба случая.
     */
    @Test
    @Timeout(10)
    void testHandle() throws ExecutionException, InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        
        CompletableFuture<String> successFuture = processor.handleSuccessOrError(false);
        assertEquals("УСПЕХ", successFuture.get(),
            "Успешный результат должен быть обработан");
        
        CompletableFuture<String> errorFuture = processor.handleSuccessOrError(true);
        assertEquals("Обработана ошибка", errorFuture.get(),
            "Ошибка должна быть обработана");
    }

    /**
     * Тест проверяет обработку тайм-аутов для CompletableFuture.
     * При превышении тайм-аута должен быть выброшен TimeoutException.
     */
    @Test
    @Timeout(10)
    void testTimeout() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Результат";
        });
        
        // Для Java 8 используем get с тайм-аутом
        assertThrows(TimeoutException.class, () -> {
            future.get(500, TimeUnit.MILLISECONDS);
        }, "Должен быть выброшен TimeoutException");
    }

    /**
     * Тест проверяет обработку результата через thenAccept.
     * AsyncTaskProcessor должен обработать результат асинхронной операции.
     */
    @Test
    @Timeout(10)
    void testThenAccept() throws InterruptedException {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        processor.processResult(42, value -> {
            counter.set(value);
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
        assertEquals(42, counter.get(),
            "Значение должно быть установлено через thenAccept");
    }
}

