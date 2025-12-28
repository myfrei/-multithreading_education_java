# Практическое задание: Конкурентные коллекции

## Описание тестов

### testCopyOnWriteArrayList
Проверяет работу `CopyOnWriteArrayList` в многопоточной среде. Список должен безопасно обрабатывать множество читателей и несколько писателей.

### testConcurrentHashMap
Проверяет работу `ConcurrentCache` в многопоточной среде. Кэш должен безопасно обрабатывать одновременные операции записи.

### testConcurrentHashMapAtomicOperations
Проверяет атомарные операции в `ConcurrentCache`. Метод `incrementCounter` должен атомарно увеличивать счетчик.

### testBlockingQueueProducerConsumer
Проверяет паттерн "Производитель-Потребитель" с `BlockingQueue`. `ProducerConsumer` должен корректно производить и потреблять элементы.

### testBlockingQueueTimeout
Проверяет работу тайм-аутов в `BlockingQueue`. При попытке добавить элемент в полную очередь с тайм-аутом должен вернуться `false`.

### testConcurrentHashMapCompute
Проверяет атомарное обновление через `compute` в `ConcurrentCache`. Метод `computeValue` должен атомарно обновлять значение.

### testConcurrentHashMapMerge
Проверяет объединение значений через `merge` в `ConcurrentCache`. Метод `mergeValue` должен атомарно объединять значения.

## Классы для реализации

- **ConcurrentCache**: Потокобезопасный кэш с использованием ConcurrentHashMap
- **ProducerConsumer**: Реализация паттерна "Производитель-Потребитель" с BlockingQueue

