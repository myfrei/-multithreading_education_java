# Практическое задание: Project Reactor

## Описание тестов

### testMonoCreation
Проверяет создание Mono со значением. `ReactiveProcessor` должен создать Mono с переданным значением.

### testFluxCreation
Проверяет создание Flux с несколькими значениями. `ReactiveProcessor` должен создать Flux с переданными значениями.

### testMapOperator
Проверяет преобразование Flux строк в верхний регистр через `map`. `ReactiveProcessor` должен преобразовать все строки в верхний регистр.

### testFilterOperator
Проверяет фильтрацию четных чисел через `filter`. `ReactiveProcessor` должен отфильтровать только четные числа.

### testFlatMap
Проверяет преобразование элементов через `flatMap`. `ReactiveProcessor` должен развернуть каждый элемент в поток.

### testMerge
Проверяет объединение двух потоков через `merge`. `ReactiveProcessor` должен объединить элементы из обоих потоков.

### testZip
Проверяет комбинирование двух потоков через `zip`. `ReactiveProcessor` должен комбинировать элементы попарно.

### testErrorHandling
Проверяет обработку ошибки через `onErrorReturn`. `ReactiveProcessor` должен вернуть значение при ошибке.

### testOnErrorResume
Проверяет обработку ошибки через `onErrorResume`. `ReactiveProcessor` должен выполнить другой поток при ошибке.

### testSubscribe
Проверяет подписку на поток и сбор элементов. Все элементы должны быть собраны в список.

## Классы для реализации

- **ReactiveProcessor**: Работа с реактивными потоками (Mono и Flux)

