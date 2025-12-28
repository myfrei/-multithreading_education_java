# Механизмы синхронизации в Java

## **Введение: Зачем нужна синхронизация?**

Представьте, что в офисе есть один принтер, и несколько сотрудников одновременно отправляют на печать свои документы. Без чёткого порядка документы могут перемешаться, страницы одного документа напечатаются посреди другого, и в итоге получится хаос. Чтобы этого избежать, нужен механизм, который позволит одному сотруднику захватить принтер, закончить печать, и только потом передать его следующему.

В многопоточном программировании общие данные (переменные, коллекции, файлы) — это и есть наш "принтер". **Синхронизация** — это набор инструментов, которые упорядочивают доступ к этим общим ресурсам, предотвращая хаос и обеспечивая целостность данных.

Сегодня мы разберём ключевые механизмы синхронизации в Java, от встроенных до мощных инструментов из пакета **`java.util.concurrent.locks`**.

---

## **Synchronized-блоки и мониторы**

### **Что такое монитор?**

Каждый объект в Java имеет встроенный "замок", который называется **монитор**. Только один поток может в любой момент времени владеть этим замком. Представьте, что у каждой комнаты (объекта) есть только один ключ. Когда поток входит в комнату, он забирает ключ с собой. Другие потоки могут подойти к двери, но войти не смогут, пока первый поток не выйдет и не вернёт ключ.

Ключевое слово **`synchronized`** — это и есть способ использовать этот встроенный замок.

### **Синхронизация методов**

Самый простой способ использовать **`synchronized`** — это пометить им весь метод.

```java
class BankAccount {
private int balance = 1000;
// Весь метод синхронизирован. Поток захватывает монитор объекта 'this'
public synchronized void deposit(int amount) {
    int temp = balance;
    temp += amount; // Операция может быть прервана другим потоком
    balance = temp;
}

public synchronized int getBalance() {
    return balance;
	}
}
```

Когда один поток вызывает **`deposit()`** или **`getBalance()`** у конкретного объекта **`BankAccount`**, он захватывает монитор этого объекта. Другой поток, пытающийся вызвать любой синхронизированный метод *того же объекта*, будет заблокирован и будет ждать, пока первый поток не завершит свою работу.

**Примечание:** Если метод статический (**`public static synchronized ...`**), то блокировка происходит на монитор класса (**`BankAccount.class`**), а не на конкретный экземпляр.

### **Синхронизация блоков**

Захватывать монитор на весь метод — это как арендовать весь конференц-зал, чтобы выпить стакан воды. Это расточительно. Часто нам нужно заблокировать только критическую секцию кода.

```java
class BankAccount {
private int balance = 1000;
private final Object lock = new Object(); // ← Специальный объект-замок
public void deposit(int amount) {
    // Синхронизируемся на отдельном объекте-замке
    synchronized (lock) { // ← Захватываем монитор объекта 'lock'
        int temp = balance;
        temp += amount;
        balance = temp;
    } // ← Монитор 'lock' освобождается здесь
}

public int getBalance() {
    synchronized (lock) {
        return balance;
    }
	}
}
```

**Важное правило:** Используйте синхронизацию на блоке с приватным финальным объектом-замком (**`private final Object lock`**). Это лучшая практика, потому что:

1. **Уменьшается область блокировки:** Только нужный код блокируется, а не весь метод.
2. **Предотвращаются дедлоки:** Внешний код не может случайно захватить ваш замок, так как он **`private`**.

---

## **Продвинутые возможности синхронизации**

Иногда встроенного **`synchronized`** недостаточно. Java предоставляет более гибкие и мощные инструменты.

### **Atomic* классы**

Представьте, что **`i++`** — это не одна операция, а три: прочитать значение, прибавить 1, записать новое. Между этими шагами другой поток может вклиниться. **`Atomic*`** классы решают эту проблему на низком уровне, используя аппаратные инструкции процессора (CAS - Compare-And-Swap).

```java
import java.util.concurrent.atomic.AtomicInteger;

class AtomicCounter {
private final AtomicInteger count = new AtomicInteger(0);

public void increment() {
    // Эта операция атомарна. Её нельзя прервать.
    count.incrementAndGet(); // ← Гарантированно безопасно
}

public int getCount() {
    return count.get();
	}
}
```

**Преимущества:**

- ✅ Обычно быстрее, чем **`synchronized`**, для простых операций.
- ✅ Помогает избежать ошибок, связанных с блокировками.

**Когда использовать:** Для простых счётчиков, флагов и других атомарных операций над одной переменной.

### **ReentrantLock**

Это более "умный" и гибкий замок по сравнению с **`synchronized`**.

**Аналогия:** Если **`synchronized`** — это простой ключ, то **`ReentrantLock`** — это электронная карта доступа. Она может:

- Позволять попытаться открыть дверь на некоторое время (**`tryLock()`**).
- Быть "честной" (**`fair`**), обслуживать потоки в порядке очереди.
- Выдавать информацию о том, кто ждёт у двери.

```java
import java.util.concurrent.locks.ReentrantLock;

class LockBasedCounter {
private final ReentrantLock lock = new ReentrantLock(); // Создаём замок
private int count = 0;
public void increment() {
    lock.lock(); // ← Захватываем замок
    try {
        count++;
    } finally {
        lock.unlock(); // ← ОБЯЗАТЕЛЬНО освобождаем в блоке finally
    }
	}
}
```

**Важное правило:** Всегда вызывайте **`unlock()`** в блоке **`finally`**. Это гарантирует, что замок будет освобождён, даже если в защищённом блоке произойдёт исключение.

### **ReadWriteLock**

Представьте себе библиотеку. Много людей могут читать книги одновременно, но только один может писать (и при этом никто не может читать). **`ReadWriteLock`** работает точно так же.

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class DataCache {
private final Map<String, String> cache = new HashMap<>();
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
public String get(String key) {
    rwLock.readLock().lock(); // ← Захватываем замок на чтение
    try {
        return cache.get(key);
    } finally {
        rwLock.readLock().unlock();
    }
}

public void put(String key, String value) {
    rwLock.writeLock().lock(); // ← Захватываем эксклюзивный замок на запись
    try {
        cache.put(key, value);
    } finally {
        rwLock.writeLock().unlock();
    }
	}
}
```

**Преимущества:**

- ✅ Значительно повышает производительность для данных, которые часто читают, но редко меняют.

### **Semaphore**

Вернёмся к аналогии с парковкой. **`Semaphore`** — это как шлагбаум на парковку с ограниченным количеством мест.

```java
import java.util.concurrent.Semaphore;

class ConnectionPool {
private final Semaphore semaphore;
// ... пул соединений ...
public ConnectionPool(int poolSize) {
    this.semaphore = new Semaphore(poolSize); // ← Создаём семафор с N "разрешениями"
}

public Connection getConnection() throws InterruptedException {
    semaphore.acquire(); // ← "Занимаем" место. Если мест нет, ждём.
    // ... выдаем соединение из пула ...
    return connection;
}

public void releaseConnection(Connection connection) {
    // ... возвращаем соединение в пул ...
    semaphore.release(); // ← "Освобождаем" место для другого потока.
	}
}
```

**Когда использовать:** Для управления доступом к пулу ограниченных ресурсов (соединения с БД, сокеты и т.д.).

### **CountDownLatch**

Это стартовый пистолет на забеге. Основной поток может запустить несколько рабочих потоков и дождаться, пока все они не закончат свою работу.

```java
import java.util.concurrent.CountDownLatch;

public class RaceDemo {
public static void main(String[] args) throws InterruptedException {
int runnerCount = 5;
CountDownLatch startSignal = new CountDownLatch(1); // Пистолет
CountDownLatch finishLine = new CountDownLatch(runnerCount); // Финишная черта    
    for (int i = 0; i < runnerCount; i++) {
        new Thread(new Runner(startSignal, finishLine)).start();
    }

    System.out.println("На старт... Внимание...");
    Thread.sleep(1000);
    startSignal.countDown(); // ← БАМ! Все потоки бегут одновременно

    System.out.println("Ждём, когда все финишируют...");
    finishLine.await(); // ← Главный поток ждёт здесь
    System.out.println("Забег окончен!");
	}
}

class Runner implements Runnable {

private final CountDownLatch startSignal;
private final CountDownLatch finishLine;

Runner(CountDownLatch startSignal, CountDownLatch finishLine) {
    this.startSignal = startSignal;
    this.finishLine = finishLine;
}

@Override
public void run() {
    try {
        startSignal.await(); // ← Ждём сигнала старта
        System.out.println(Thread.currentThread().getName() + " бежит!");
        Thread.sleep((long) (Math.random() * 1000));
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        finishLine.countDown(); // ← Пересекли финишную черту
    }
	}
}
```

**Сравнение механизмов синхронизации**

| Характеристика | `synchronized` | `ReentrantLock` | `Atomic*` | `ReadWriteLock` | `Semaphore` | `CountDownLatch` |
| --- | --- | --- | --- | --- | --- | --- |
| **Тип блокировки** | Встроенный монитор | Явный замок | Блокировка на уровне CPU (CAS) | Два замка (чтение/запись) | Счётчик разрешений | Счётчик обратного отсчёта |
| **Гибкость** | Низкая | Высокая (тайм-ауты, честность) | Низкая (только простые операции) | Средняя | Средняя | Низкая (одноразовый) |
| **Основной сценарий** | Простая защита критической секции | Сложные сценарии, тайм-ауты | Атомарные операции с одной переменной | Защита данных, читаемых чаще, чем пишутся | Управление пулом ресурсов | Синхронизация запуска/завершения потоков |

## **Заключение**

Поздравляю, вы теперь владеете целым арсеналом инструментов для синхронизации в Java! Выбор правильного инструмента — это ключ к созданию эффективных и надёжных многопоточных приложений.

**Ключевые выводы:**

1. Начинайте с простого. Для базовой защиты данных часто достаточно **`synchronized`**.
2. Используйте **`Atomic*`** для простых счётчиков и флагов — это самый быстрый способ.
3. Когда **`synchronized`** не хватает гибкости (нужны тайм-ауты, честность), переходите на **`ReentrantLock`**.
4. Если у вас много читателей и мало писателей, **`ReadWriteLock`** может кардинально повысить производительность.
5. Для управления доступом к пулу ресурсов идеален **`Semaphore`**.
6. Для координации запуска или ожидания завершения группы потоков используйте **`CountDownLatch`**.

Понимание этих механизмов и их компромиссов отличает Junior-разработчика от Senior. Теперь вы готовы проектировать сложные, надёжные и высокопроизводительные системы. Удачи в покорении многопоточности