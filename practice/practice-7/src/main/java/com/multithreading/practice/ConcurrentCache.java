package com.multithreading.practice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потокобезопасный кэш с использованием ConcurrentHashMap.
 * 
 * Задание: Реализуйте методы так, чтобы все тесты проходили.
 * 
 * Подсказки:
 * - ConcurrentHashMap использует тонкую блокировку на уровне сегментов
 * - computeIfAbsent() атомарно добавляет значение, если ключа нет
 * - compute() атомарно обновляет значение
 * - merge() атомарно объединяет значения
 */
public class ConcurrentCache {
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();
    
    /**
     * Помещает значение в кэш.
     * 
     * @param key ключ
     * @param value значение
     */
    public void put(String key, Integer value) {
        cache.put(key, value);
    }
    
    /**
     * Получает значение из кэша.
     * 
     * @param key ключ
     * @return значение или null, если ключа нет
     */
    public Integer get(String key) {
        return cache.get(key);
    }
    
    /**
     * Возвращает размер кэша.
     * 
     * @return количество элементов в кэше
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Атомарно увеличивает счетчик для ключа.
     * 
     * @param key ключ счетчика
     * @return новое значение счетчика
     */
    public int incrementCounter(String key) {
        AtomicInteger counter = counters.computeIfAbsent(key, k -> new AtomicInteger(0));
        return counter.incrementAndGet();
    }
    
    /**
     * Получает счетчик для ключа.
     * 
     * @param key ключ счетчика
     * @return счетчик или null, если ключа нет
     */
    public AtomicInteger getCounter(String key) {
        return counters.get(key);
    }
    
    /**
     * Атомарно обновляет значение для ключа.
     * 
     * @param key ключ
     * @param updateFunction функция обновления
     * @return новое значение
     */
    public Integer computeValue(String key, 
                                java.util.function.BiFunction<String, Integer, Integer> updateFunction) {
        return cache.compute(key, (k, v) -> updateFunction.apply(k, v));
    }
    
    /**
     * Атомарно объединяет значения для ключа.
     * 
     * @param key ключ
     * @param value значение для объединения
     * @param mergeFunction функция объединения
     * @return новое значение
     */
    public Integer mergeValue(String key, 
                             Integer value,
                             java.util.function.BiFunction<Integer, Integer, Integer> mergeFunction) {
        return cache.merge(key, value, mergeFunction);
    }
}

