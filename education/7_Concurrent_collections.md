# Конкурентные коллекции

# **Copy-on-write array list, ConcurrentHashMap, BlockingQueue**

## **Введение в конкурентные коллекции**

Представьте, что у вас есть общий документ, с которым одновременно работают несколько человек. Без правильных инструментов это превратится в хаос: одни будут стирать то, что написали другие, возникнут конфликты и путаница. В мире многопоточного программирования происходит то же самое, когда несколько потоков пытаются одновременно работать с общей коллекцией данных.

**Конкурентные коллекции** — это специальные структуры данных из пакета **`java.util.concurrent`**, разработанные для безопасной работы в многопоточной среде. В отличие от обычных коллекций, они предоставляют механизмы синхронизации, которые предотвращают состояние гонки (race condition) и обеспечивают согласованность данных при одновременном доступе из нескольких потоков.

Давайте разберёмся, почему обычные коллекции не подходят для многопоточной среды:

```java
List<String> unsafeList = new ArrayList<>();
// Если несколько потоков одновременно вызывают этот метод, возникнет ConcurrentModificationException
// или данные могут быть повреждены
public void addItem(String item) {
	unsafeList.add(item); // ← Не потокобезопасная операция
}
```

**Важное правило:** Обычные коллекции из **`java.util`** (ArrayList, HashMap, HashSet и др.) не являются потокобезопасными и могут приводить к непредсказуемым результатам при одновременном доступе из нескольких потоков.

Теперь давайте рассмотрим три ключевые конкурентные коллекции, которые решают эти проблемы.

## **CopyOnWriteArrayList**

### **Что такое CopyOnWriteArrayList?**

Представьте себе доску объявлений, на которой размещена важная информация. Когда нужно добавить новое объявление, вместо того чтобы пытаться вписать его на уже заполненную доску (мешая тем, кто её читает), вы делаете полную копию доски, добавляете объявление на копию, а затем заменяете старую доску новой. Именно по такому принципу работает **`CopyOnWriteArrayList`**.

**CopyOnWriteArrayList** — это потокобезопасная реализация списка, которая использует стратегию "копирование при записи" (copy-on-write). При каждой модификации коллекции (добавление, удаление, изменение элемента) создаётся новая копия внутреннего массива, а все операции чтения работают с неизменяемой версией массива.

### **Как это работает?**

```java
// Создание CopyOnWriteArrayList
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
// Поток 1: добавление элементов
list.add("Элемент 1"); // ← Создаётся новый массив с этим элементом
list.add("Элемент 2"); // ← Создаётся ещё один новый массив с обоими элементами

// Поток 2: чтение элементов (не блокируется)
for (String item : list) { // ← Работает с копией массива
System.out.println(item);
}
```

### **Преимущества и недостатки**

**Преимущества:**

- ✅ Операции чтения не блокируются и выполняются очень быстро
- ✅ Итератор не бросает **`ConcurrentModificationException`**
- ✅ Гарантирует атомарность операций

**Недостатки:**

- ❌ Операции записи требуют создания полной копии массива
- ❌ Не подходит для коллекций с частыми модификациями
- ❌ Потребляет больше памяти из-за создания копий

### **Когда использовать CopyOnWriteArrayList?**

**`CopyOnWriteArrayList`** идеально подходит для сценариев, где:

- Чтение происходит значительно чаще, чем запись
- Количество элементов невелико (обычно менее 1000)
- Необходима консистентность данных во время итерации

**Пример использования:**

```java
// Список слушателей событий в GUI-приложении
CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
// Добавление слушателей (редкая операция)
public void addListener(EventListener listener) {
listeners.add(listener); // ← Создаётся новая копия массива
}

// Уведомление слушателей (частая операция)
public void notifyListeners(Event event) {
	for (EventListener listener : listeners) { // ← Безопасная итерация
		listener.onEvent(event); // ← Не бросает ConcurrentModificationException
	}
}
```

## **ConcurrentHashMap**

### **Что такое ConcurrentHashMap?**

Представьте себе большую библиотеку, где много посетителей одновременно ищут книги. Вместо того чтобы выстраивать их в одну очередь к единственному библиотекарю, библиотека разделена на секции, и в каждой секции свой библиотекарь. Посетители могут работать в разных секциях одновременно, не мешая друг другу.

**ConcurrentHashMap** — это потокобезопасная реализация интерфейса **`Map`**, которая использует сегментацию для обеспечения высокой производительности при одновременном доступе из нескольких потоков.

### **Как это работает?**

До Java 8 **`ConcurrentHashMap`** делилась на сегменты (обычно 16), каждый из которых имел свою собственную блокировку. Начиная с Java 8, реализация была улучшена и теперь использует более тонкую блокировку на уровне "корзин" (buckets).

```java
// Создание ConcurrentHashMap
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
// Поток 1: добавление элементов
map.put("Ключ1", 1); // ← Блокируется только конкретная корзина
map.put("Ключ2", 2); // ← Может выполняться параллельно с операцией над другой корзиной

// Поток 2: чтение элементов (обычно не блокируется)
Integer value = map.get("Ключ1"); // ← Почти всегда неблокирующая операция
```

### **Преимущества и недостатки**

**Преимущества:**

- ✅ Высокая производительность при одновременном доступе
- ✅ Эффективное масштабирование с увеличением количества потоков
- ✅ Не блокирует всю коллекцию при выполнении операций

**Недостатки:**

- ❌ Потребляет больше памяти, чем обычная HashMap
- ❌ Некоторые операции (например, size()) могут быть менее точными и медленными
- ❌ Итераторы отражают состояние коллекции на момент создания и не бросают **`ConcurrentModificationException`**

### **Атомарные операции**

ConcurrentHashMap предоставляет мощные атомарные операции, которые особенно полезны в многопоточной среде:

```java
ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
// Атомарное добавление с вычислением
scores.computeIfAbsent("Alice", key -> 0); // ← Если ключа нет, добавит со значением 0

// Атомарное обновление значения
scores.computeIfPresent("Alice", (key, value) -> value + 10); // ← Если ключ есть, увеличит значение на 10

// Атомарная замена значения
scores.replace("Alice", 10, 20); // ← Заменит значение, только если текущее значение равно 10

// Атомарное удаление по значению
scores.remove("Alice", 20); // ← Удалит, только если значение равно 20
```

### **Когда использовать ConcurrentHashMap?**

**`ConcurrentHashMap`** идеально подходит для:

- Кэшей в многопоточных приложениях
- Сценариев с высокой конкуренцией за доступ к данным
- Ситуаций, когда требуется высокая производительность чтения и записи

---

## **BlockingQueue**

### **Что такое BlockingQueue?**

Представьте себе очередь в кассу магазина. Если кассир занят, покупатели встают в очередь и ждут своей очереди. Если в очереди нет покупателей, кассир может ждать, пока кто-нибудь подойдёт. Если очередь заполнена до предела, новые покупатели не могут в неё встать и ждут, пока освободится место.

**BlockingQueue** — это интерфейс очереди, который поддерживает операции, которые ожидают, пока очередь не станет доступной для выполнения операции. Например, операция извлечения элемента будет блокировать поток, если очередь пуста, а операция добавления — если очередь заполнена.

### **Как это работает?**

```java
// Создание ограниченной BlockingQueue с ёмкостью 5
BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
// Поток-производитель
try {
	queue.put("Элемент"); // ← Блокируется, если очередь заполнена
} catch (InterruptedException e) {
	Thread.currentThread().interrupt();
}

// Поток-потребитель
try {
	String element = queue.take(); // ← Блокируется, если очередь пуста
} catch (InterruptedException e) {
	Thread.currentThread().interrupt();
}
```

### **Реализации BlockingQueue**

Java предоставляет несколько реализаций **`BlockingQueue`**:

1. **ArrayBlockingQueue** — ограниченная очередь на основе массива
2. **LinkedBlockingQueue** — может быть ограниченной или неограниченной
3. **PriorityBlockingQueue** — упорядочивает элементы по их естественному порядку или компаратору
4. **SynchronousQueue** — не содержит элементов, каждый операция put ждёт соответствующей операции take

### **Пример использования BlockingQueue**

**`BlockingQueue`** идеально подходит для реализации паттерна "Производитель-Потребитель":

```java
class ProducerConsumerExample {
private static final int CAPACITY = 5;
private static final BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(CAPACITY);
static class Producer implements Runnable {
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Производитель производит: " + i);
                queue.put(i); // ← Блокируется, если очередь заполнена
                Thread.sleep(100); // ← Имитация работы
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

static class Consumer implements Runnable {
    @Override
    public void run() {
        try {
            while (true) {
                Integer value = queue.take(); // ← Блокируется, если очередь пуста
                System.out.println("Потребитель потребляет: " + value);
                Thread.sleep(200); // ← Имитация работы
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(new Producer());
    executor.execute(new Consumer());
    executor.shutdown();
	}
}

```

## **Сравнение конкурентных коллекций**

Давайте сравним рассмотренные коллекции по ключевым характеристикам:

| Характеристика | CopyOnWriteArrayList | ConcurrentHashMap | BlockingQueue |
| --- | --- | --- | --- |
| **Основное назначение** | Чтение намного чаще записи | Общий доступ к карте | Синхронизация потоков |
| **Производительность чтения** | Очень высокая | Высокая | Зависит от реализации |
| **Производительность записи** | Низкая | Высокая | Зависит от реализации |
| **Потребление памяти** | Высокое | Среднее | Зависит от реализации |
| **Итератор** | Не бросает ConcurrentModificationException | Не бросает ConcurrentModificationException | Зависит от реализации |
| **Блокировка** | Только при записи | Тонкая блокировка | Блокирующие операции |

## **Практическая реализация: Многопоточный кэш**

Давайте создадим простой многопоточный кэш, который использует **`ConcurrentHashMap`** для хранения данных и **`CopyOnWriteArrayList`** для отслеживания слушателей изменений:

```java
public class ConcurrentCache<K, V> {
private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
private final CopyOnWriteArrayList<CacheListener<K, V>> listeners = new CopyOnWriteArrayList<>();

// Интерфейс для слушателей изменений кэша
public interface CacheListener<K, V> {
    void onPut(K key, V value);
    void onRemove(K key);
    void onClear();
}

// Добавление слушателя
public void addListener(CacheListener<K, V> listener) {
    listeners.add(listener); // ← Безопасно для многопоточного доступа
}

// Удаление слушателя
public void removeListener(CacheListener<K, V> listener) {
    listeners.remove(listener); // ← Безопасно для многопоточного доступа
}

// Помещение значения в кэш
public void put(K key, V value) {
    cache.put(key, value); // ← Потокобезопасная операция
    notifyListeners(listener -> listener.onPut(key, value));
}

// Получение значения из кэша
public V get(K key) {
    return cache.get(key); // ← Потокобезопасная операция
}

// Удаление значения из кэша
public V remove(K key) {
    V removedValue = cache.remove(key); // ← Потокобезопасная операция
    if (removedValue != null) {
        notifyListeners(listener -> listener.onRemove(key));
    }
    return removedValue;
}

// Очистка кэша
public void clear() {
    cache.clear(); // ← Потокобезопасная операция
    notifyListeners(CacheListener::onClear);
}

// Размер кэша
public int size() {
    return cache.size(); // ← Потокобезопасная операция
}

// Уведомление всех слушателей
private void notifyListeners(Consumer<CacheListener<K, V>> action) {
    for (CacheListener<K, V> listener : listeners) { // ← Безопасная итерация
        action.accept(listener);
    }
}

// Пример использования
public static void main(String[] args) throws InterruptedException {
    ConcurrentCache<String, Integer> cache = new ConcurrentCache<>();

    // Добавление слушателя
    cache.addListener(new CacheListener<String, Integer>() {
        @Override
        public void onPut(String key, Integer value) {
            System.out.println("Добавлено: " + key + " = " + value);
        }

        @Override
        public void onRemove(String key) {
            System.out.println("Удалено: " + key);
        }

        @Override
        public void onClear() {
            System.out.println("Кэш очищен");
        }
    });

    // Создание нескольких потоков для работы с кэшем
    ExecutorService executor = Executors.newFixedThreadPool(3);

    // Поток 1: добавление элементов
    executor.submit(() -> {
        for (int i = 0; i < 5; i++) {
            cache.put("Ключ" + i, i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    });

    // Поток 2: чтение элементов
    executor.submit(() -> {
        for (int i = 0; i < 5; i++) {
            Integer value = cache.get("Ключ" + i);
            System.out.println("Получено: " + value);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    });

    // Поток 3: удаление элементов
    executor.submit(() -> {
        try {
            Thread.sleep(300);
            cache.remove("Ключ2");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });

    executor.shutdown();
    executor.awaitTermination(2, TimeUnit.SECONDS);
	}
}
```

## **Заключение**

Поздравляю, вы только что познакомились с тремя важнейшими конкурентными коллекциями в Java! Давайте подведём итоги:

1. **CopyOnWriteArrayList** идеально подходит для сценариев с преобладанием операций чтения над записью, где важна консистентность данных во время итерации.
2. **ConcurrentHashMap** обеспечивает высокую производительность при одновременном доступе из нескольких потоков и предоставляет мощные атомарные операции.
3. **BlockingQueue** отлично подходит для реализации паттерна "Производитель-Потребитель" и синхронизации работы между потоками.

Выбор правильной конкурентной коллекции зависит от конкретной задачи:

- Используйте **`CopyOnWriteArrayList`**, когда у вас много читателей и мало писателей.
- Используйте **`ConcurrentHashMap`**, когда требуется быстрый доступ к данным из нескольких потоков.
- Используйте **`BlockingQueue`**, когда нужно синхронизировать работу потоков через обмен данными.

Теперь вы готовы создавать эффективные и безопасные многопоточные приложения с использованием этих мощных инструментов из пакета **`java.util.concurrent`**. Удачи в ваших проектах

---

## **Практическое задание**

Для закрепления материала выполните практическое задание в проекте `practice/practice-7`.

**Задача:** Реализуйте классы и методы так, чтобы все unit-тесты в `ConcurrentCollectionsTest.java` проходили.

**Требования:**
1. Используйте `CopyOnWriteArrayList` для сценариев "много читателей, мало писателей"
2. Используйте `ConcurrentHashMap` для потокобезопасного кэша
3. Используйте `BlockingQueue` для паттерна "Производитель-Потребитель"
4. Правильно используйте атомарные операции в `ConcurrentHashMap` (`compute`, `merge`)

**Инструкция:**
1. Перейдите в директорию `practice/practice-7`
2. Запустите тесты: `mvn test`
3. Реализуйте недостающие классы и методы, чтобы все тесты проходили
4. Не изменяйте сами тесты!

**Подсказки:**
- `CopyOnWriteArrayList` создает копию при записи, идеален для частых чтений
- `ConcurrentHashMap` использует тонкую блокировку на уровне сегментов
- `BlockingQueue.put()` блокируется, если очередь полна, `take()` блокируется, если очередь пуста
- Используйте `computeIfAbsent`, `compute`, `merge` для атомарных операций в `ConcurrentHashMap`