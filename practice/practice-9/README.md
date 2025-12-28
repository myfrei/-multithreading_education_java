# Практическое задание: Java Memory Model

## Описание тестов

### testVolatileVisibility
Проверяет использование `volatile` для обеспечения видимости изменений между потоками. `VolatileFlag` должен гарантировать, что изменения видны всем потокам.

### testSynchronizedHappensBefore
Проверяет использование `synchronized` для гарантии happens-before. `SynchronizedCounter` должен гарантировать видимость изменений через synchronized блоки.

### testDoubleCheckedLocking
Проверяет правильную реализацию Double-Checked Locking с `volatile`. `Singleton` должен создавать только один экземпляр даже в многопоточной среде.

### testThreadStartHappensBefore
Проверяет гарантию happens-before через `Thread.start()`. Значения, установленные до `start()`, должны быть видны в новом потоке.

### testThreadJoinHappensBefore
Проверяет гарантию happens-before через `Thread.join()`. Значения, установленные в потоке, должны быть видны после `join()`.

### testVolatileVsSynchronized
Проверяет разницу между `volatile` и `synchronized`. `VolatileCounter` демонстрирует, что `volatile` обеспечивает видимость, но не атомарность.

## Классы для реализации

- **VolatileFlag**: Класс с volatile флагом для демонстрации видимости
- **SynchronizedCounter**: Счетчик с использованием synchronized для гарантии happens-before
- **Singleton**: Singleton с правильным Double-Checked Locking
- **DataHolder**: Класс для демонстрации happens-before через Thread.start()
- **ResultHolder**: Класс для демонстрации happens-before через Thread.join()
- **VolatileCounter**: Счетчик с volatile для демонстрации ограничений volatile

