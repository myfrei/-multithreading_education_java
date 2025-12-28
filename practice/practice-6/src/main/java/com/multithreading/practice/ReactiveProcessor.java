package com.multithreading.practice;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Класс для работы с реактивными потоками (Project Reactor).
 * 
 * Задание: Реализуйте методы так, чтобы все тесты проходили.
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
public class ReactiveProcessor {
    
    /**
     * Создает Mono со значением.
     * 
     * @param value значение для Mono
     * @return Mono со значением
     */
    public Mono<String> createMono(String value) {
        return Mono.just(value);
    }
    
    /**
     * Создает Flux с несколькими значениями.
     * 
     * @param values значения для Flux
     * @return Flux со значениями
     */
    public Flux<Integer> createFlux(Integer... values) {
        return Flux.just(values);
    }
    
    /**
     * Преобразует Flux строк в верхний регистр.
     * 
     * @param input входной Flux строк
     * @return Flux с преобразованными строками
     */
    public Flux<String> transformToUpperCase(Flux<String> input) {
        return input.map(String::toUpperCase);
    }
    
    /**
     * Отфильтровывает только четные числа.
     * 
     * @param input входной Flux чисел
     * @return Flux с отфильтрованными числами
     */
    public Flux<Integer> filterEvenNumbers(Flux<Integer> input) {
        return input.filter(n -> n % 2 == 0);
    }
    
    /**
     * Преобразует каждый элемент в поток через flatMap.
     * 
     * @param input входной Flux чисел
     * @return Flux с развернутыми элементами
     */
    public Flux<Integer> expandWithFlatMap(Flux<Integer> input) {
        return input.flatMap(n -> Flux.range(1, n));
    }
    
    /**
     * Объединяет два потока через merge.
     * 
     * @param flux1 первый поток
     * @param flux2 второй поток
     * @return объединенный поток
     */
    public Flux<String> mergeFluxes(Flux<String> flux1, Flux<String> flux2) {
        return Flux.merge(flux1, flux2);
    }
    
    /**
     * Комбинирует два потока через zip.
     * 
     * @param names поток имен
     * @param ages поток возрастов
     * @return поток с комбинированными строками
     */
    public Flux<String> zipFluxes(Flux<String> names, Flux<Integer> ages) {
        return Flux.zip(names, ages)
            .map(tuple -> tuple.getT1() + " is " + tuple.getT2());
    }
    
    /**
     * Обрабатывает ошибку через onErrorReturn.
     * 
     * @param input входной поток
     * @param errorValue значение при ошибке
     * @return поток с обработанной ошибкой
     */
    public Flux<String> handleErrorWithReturn(Flux<String> input, String errorValue) {
        return input.onErrorReturn(errorValue);
    }
    
    /**
     * Обрабатывает ошибку через onErrorResume.
     * 
     * @param input входной поток
     * @param recovery поток для восстановления
     * @return поток с обработанной ошибкой
     */
    public Flux<String> handleErrorWithResume(Flux<String> input, Flux<String> recovery) {
        return input.onErrorResume(ex -> recovery);
    }
}

