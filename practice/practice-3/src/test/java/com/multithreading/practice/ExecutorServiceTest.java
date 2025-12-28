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
 * 1. Реализуйте TaskProcessor, который обрабатывает задачи через ExecutorService
 * 2. Реализуйте параллельную обработку списка задач через ParallelProcessor
 * 3. Реализуйте использование виртуальных потоков через VirtualThreadExecutor (Java 21+)
 * 4. Реализуйте правильное завершение ExecutorService
 * 
 * Подсказки:
 * - Используйте Executors.newFixedThreadPool() для создания пула потоков
 * - Не забывайте вызывать shutdown() и awaitTermination()
 * - Для виртуальных потоков используйте Executors.newVirtualThreadPerTaskExecutor()
 */
class ExecutorServiceTest {

    /**
     * Тест проверяет работу TaskProcessor с фиксированным пулом потоков.
     * TaskProcessor должен обрабатывать задачи и корректно завершаться.
     */
    @Test
    @Timeout(10)
    void testFixedThreadPool() throws InterruptedException, ExecutionException {
        TaskProcessor processor = new TaskProcessor(5);
        List<Future<Integer>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            Future<Integer> future = processor.processTask(() -> {
                Thread.sleep(100);
                return taskId * 2;
            });
            futures.add(future);
        }
        
        for (int i = 0; i < futures.size(); i++) {
            assertEquals(i * 2, futures.get(i).get(),
                "Результат задачи должен быть корректным");
        }
        
        assertTrue(processor.shutdownGracefully(5, TimeUnit.SECONDS),
            "ExecutorService должен завершиться");
        assertTrue(processor.isTerminated(),
            "ExecutorService должен быть завершен");
    }

    /**
     * Тест проверяет параллельную обработку списка элементов.
     * ParallelProcessor должен обрабатывать все элементы параллельно.
     */
    @Test
    @Timeout(10)
    void testParallelProcessing() throws InterruptedException {
        ParallelProcessor processor = new ParallelProcessor();
        List<Integer> input = List.of(1, 2, 3, 4, 5);
        
        List<Integer> results = processor.processInParallel(input, value -> value * 2, 3);
        
        assertEquals(5, results.size(), "Должно быть обработано 5 элементов");
        assertTrue(results.containsAll(List.of(2, 4, 6, 8, 10)),
            "Результаты должны быть удвоены");
    }

    /**
     * Тест проверяет работу виртуальных потоков (требует Java 21+).
     * VirtualThreadExecutor должен выполнить все задачи в виртуальных потоках.
     */
    @Test
    @Timeout(10)
    void testVirtualThreads() throws InterruptedException, ExecutionException {
        VirtualThreadExecutor executor = new VirtualThreadExecutor();
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        int completed = executor.executeWithVirtualThreads(tasks);
        
        assertEquals(100, completed,
            "Все виртуальные потоки должны выполниться");
    }

    /**
     * Тест проверяет корректное завершение ExecutorService.
     * TaskProcessor должен дождаться завершения всех задач перед завершением.
     */
    @Test
    @Timeout(10)
    void testShutdownGracefully() throws InterruptedException {
        TaskProcessor processor = new TaskProcessor(2);
        CountDownLatch latch = new CountDownLatch(5);
        
        for (int i = 0; i < 5; i++) {
            processor.processTask(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
                return 0;
            });
        }
        
        assertTrue(processor.shutdownGracefully(5, TimeUnit.SECONDS),
            "ExecutorService должен завершиться");
        
        assertTrue(processor.isTerminated(),
            "ExecutorService должен быть завершен");
    }

    /**
     * Тест проверяет обработку исключений в задачах.
     * Исключения должны быть обернуты в ExecutionException.
     */
    @Test
    @Timeout(10)
    void testExceptionHandling() throws InterruptedException {
        TaskProcessor processor = new TaskProcessor(1);
        
        Future<Integer> future = processor.processTask(() -> {
            throw new RuntimeException("Тестовая ошибка");
        });
        
        assertThrows(ExecutionException.class, () -> future.get(),
            "Должно быть выброшено ExecutionException");
        
        processor.shutdownGracefully(1, TimeUnit.SECONDS);
    }

    /**
     * Тест проверяет обработку тайм-аутов при выполнении задач.
     * При превышении тайм-аута должен быть выброшен TimeoutException.
     */
    @Test
    @Timeout(10)
    void testTimeout() throws InterruptedException {
        TaskProcessor processor = new TaskProcessor(1);
        
        Future<String> future = processor.processTask(() -> {
            Thread.sleep(2000);
            return "Результат";
        });
        
        assertThrows(TimeoutException.class, () -> {
            future.get(500, TimeUnit.MILLISECONDS);
        }, "Должен быть выброшен TimeoutException");
        
        future.cancel(true);
        processor.shutdownGracefully(1, TimeUnit.SECONDS);
    }
}

