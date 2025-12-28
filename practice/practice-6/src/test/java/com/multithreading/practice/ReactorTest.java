package com.multithreading.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задача: Реализуйте классы и методы так, чтобы все тесты проходили.
 * 
 * Требования:
 * 1. Реализуйте ReactiveProcessor для работы с Flux и Mono потоками
 * 2. Используйте операторы map, filter, flatMap
 * 3. Комбинируйте потоки через merge, zip
 * 4. Обрабатывайте ошибки через onErrorReturn, onErrorResume
 * 
 * Подсказки:
 * - Mono.just() создает Mono с одним значением
 * - Flux.just() создает Flux с несколькими значениями
 * - map() преобразует каждый элемент в потоке
 * - filter() отфильтровывает элементы по условию
 * - flatMap() преобразует элемент в поток и "разворачивает" его
 * - Flux.merge() объединяет несколько потоков
 * - Flux.zip() комбинирует элементы из потоков попарно
 * - onErrorReturn() возвращает значение при ошибке
 * - onErrorResume() выполняет другой поток при ошибке
 */
class ReactorTest {

    /**
     * Тест проверяет создание Mono со значением.
     * ReactiveProcessor должен создать Mono с переданным значением.
     */
    @Test
    @Timeout(5)
    void testMonoCreation() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Mono<String> mono = processor.createMono("Hello");
        
        StepVerifier.create(mono)
            .expectNext("Hello")
            .verifyComplete();
    }

    /**
     * Тест проверяет создание Flux с несколькими значениями.
     * ReactiveProcessor должен создать Flux с переданными значениями.
     */
    @Test
    @Timeout(5)
    void testFluxCreation() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<Integer> flux = processor.createFlux(1, 2, 3, 4, 5);
        
        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4, 5)
            .verifyComplete();
    }

    /**
     * Тест проверяет преобразование Flux строк в верхний регистр через map.
     * ReactiveProcessor должен преобразовать все строки в верхний регистр.
     */
    @Test
    @Timeout(5)
    void testMapOperator() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<String> flux = processor.transformToUpperCase(Flux.just("hello", "world"));
        
        StepVerifier.create(flux)
            .expectNext("HELLO", "WORLD")
            .verifyComplete();
    }

    /**
     * Тест проверяет фильтрацию четных чисел через filter.
     * ReactiveProcessor должен отфильтровать только четные числа.
     */
    @Test
    @Timeout(5)
    void testFilterOperator() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<Integer> flux = processor.filterEvenNumbers(Flux.just(1, 2, 3, 4, 5, 6));
        
        StepVerifier.create(flux)
            .expectNext(2, 4, 6)
            .verifyComplete();
    }

    /**
     * Тест проверяет преобразование элементов через flatMap.
     * ReactiveProcessor должен развернуть каждый элемент в поток.
     */
    @Test
    @Timeout(5)
    void testFlatMap() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<Integer> flux = processor.expandWithFlatMap(Flux.just(1, 2, 3));
        
        StepVerifier.create(flux)
            .expectNextCount(6) // 1 + 2 + 3 = 6 элементов
            .verifyComplete();
    }

    /**
     * Тест проверяет объединение двух потоков через merge.
     * ReactiveProcessor должен объединить элементы из обоих потоков.
     */
    @Test
    @Timeout(5)
    void testMerge() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<String> flux1 = Flux.just("A", "B");
        Flux<String> flux2 = Flux.just("C", "D");
        Flux<String> merged = processor.mergeFluxes(flux1, flux2);
        
        StepVerifier.create(merged)
            .expectNextCount(4)
            .verifyComplete();
    }

    /**
     * Тест проверяет комбинирование двух потоков через zip.
     * ReactiveProcessor должен комбинировать элементы попарно.
     */
    @Test
    @Timeout(5)
    void testZip() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<String> names = Flux.just("Alice", "Bob");
        Flux<Integer> ages = Flux.just(25, 30);
        Flux<String> zipped = processor.zipFluxes(names, ages);
        
        StepVerifier.create(zipped)
            .expectNext("Alice is 25", "Bob is 30")
            .verifyComplete();
    }

    /**
     * Тест проверяет обработку ошибки через onErrorReturn.
     * ReactiveProcessor должен вернуть значение при ошибке.
     */
    @Test
    @Timeout(5)
    void testErrorHandling() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<String> flux = Flux.just("A", "B")
            .concatWith(Flux.error(new RuntimeException("Ошибка")));
        Flux<String> result = processor.handleErrorWithReturn(flux, "Error");
        
        StepVerifier.create(result)
            .expectNext("A", "B", "Error")
            .verifyComplete();
    }

    /**
     * Тест проверяет обработку ошибки через onErrorResume.
     * ReactiveProcessor должен выполнить другой поток при ошибке.
     */
    @Test
    @Timeout(5)
    void testOnErrorResume() {
        ReactiveProcessor processor = new ReactiveProcessor();
        Flux<String> flux = Flux.just("A")
            .concatWith(Flux.error(new RuntimeException("Ошибка")));
        Flux<String> recovery = Flux.just("Recovered");
        Flux<String> result = processor.handleErrorWithResume(flux, recovery);
        
        StepVerifier.create(result)
            .expectNext("A", "Recovered")
            .verifyComplete();
    }

    /**
     * Тест проверяет подписку на поток и сбор элементов.
     * Все элементы должны быть собраны в список.
     */
    @Test
    @Timeout(5)
    void testSubscribe() throws InterruptedException {
        List<String> collected = new ArrayList<>();
        Flux<String> flux = Flux.just("A", "B", "C");
        
        flux.subscribe(collected::add);
        Thread.sleep(100); // Даем время на выполнение
        
        assertEquals(3, collected.size());
        assertTrue(collected.containsAll(List.of("A", "B", "C")));
    }
}

