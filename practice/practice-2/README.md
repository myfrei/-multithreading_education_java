# Практическое задание: Synchronized и механизмы синхронизации

## Описание тестов

### testSynchronizedCounter
Проверяет потокобезопасный счетчик с использованием `synchronized`. `SynchronizedCounter` должен корректно работать в многопоточной среде.

### testAtomicCounter
Проверяет потокобезопасный счетчик с использованием `AtomicInteger`. `AtomicCounter` должен использовать атомарные операции.

### testReentrantLockCounter
Проверяет счетчик с использованием `ReentrantLock`. `LockCounter` должен использовать явные блокировки.

### testReadWriteLockCache
Проверяет кэш с использованием `ReadWriteLock`. `ThreadSafeCache` должен позволять множественные одновременные чтения.

### testSemaphoreResourcePool
Проверяет пул ресурсов с использованием `Semaphore`. `ResourcePool` должен контролировать количество одновременных доступов к ресурсу.

### testCountDownLatch
Проверяет координацию потоков через `CountDownLatch`. Все задачи должны завершиться перед продолжением выполнения.

## Классы для реализации

- **SynchronizedCounter**: Потокобезопасный счетчик с использованием synchronized
- **AtomicCounter**: Потокобезопасный счетчик с использованием AtomicInteger
- **LockCounter**: Счетчик с использованием ReentrantLock
- **ThreadSafeCache**: Кэш с использованием ReadWriteLock
- **ResourcePool**: Пул ресурсов с использованием Semaphore

