# ForkJoinPool

## **Введение в ForkJoinPool**

В предыдущей лекции мы говорили о **`ExecutorService`** как об универсальном менеджере "поваров" (потоков) в нашем ресторане. Этот подход отлично работает, когда у нас много независимых заказов. Но что, если у нас один гигантский заказ, например, приготовить свадебный торт на 500 человек? Один повар справится очень долго. Десять поваров могут просто мешать друг другу на одной маленькой кухне.

Оптимальное решение — разделить торт на части: один делает коржи, другой — крем, третий — украшение. А когда кто-то закончит свою часть раньше, он может подмочь тому, кто ещё занят. Именно этот принцип "разделяй и властвуй" лёг в основу **ForkJoinPool**.

**ForkJoinPool** — это специализированная реализация **`ExecutorService`**, предназначенная для эффективного выполнения рекурсивных задач, которые можно разбить на более мелкие подзадачи. Его главная "суперсила" — алгоритм "work-stealing" (кража работы).

## **Основы ForkJoinPool**

Давайте разберёмся, как работает этот механизм и чем он отличается от обычного пула потоков.

### **Что такое "work-stealing"?**

Представьте себе команду обработчиков документов. У каждого есть своя стопка бумаг. Если один сотрудник быстро закончил свою стопку, он не будет сидеть сложа руки. Он悄悄но "украдёт" часть стопки у самого занятого коллеги.

В **`ForkJoinPool`** каждый рабочий поток имеет свою очередь задач (deque). Когда поток завершает свои задачи, он не простаивает, а "крадёт" задачи из хвоста очереди других потоков. Это обеспечивает максимальную загрузку всех ядер процессора.

**Важное правило:** **`ForkJoinPool`** наиболее эффективен для **CPU-bound** задач, которые можно распараллелить, например, математические вычисления, обработка больших массивов данных, обход древовидных структур.

### **Базовые концепции: RecursiveTask и RecursiveAction**

Для работы с **`ForkJoinPool`** вам нужно создать задачу, которая наследуется от одного из двух классов:

- **`RecursiveTask<V>`**: Задача, которая возвращает результат типа **`V`**.
- **`RecursiveAction`**: Задача, которая не возвращает результат (аналогично **`Runnable`**).

Основной метод, который вы должны реализовать — **`compute()`**. В нём и содержится вся логика разделения и выполнения задачи.

---

## **Практические примеры использования ForkJoinPool**

Классический пример для демонстрации **`ForkJoinPool`** — это суммирование элементов большого массива. Вместо того чтобы идти по массиву последовательно, мы разделим его на две половины, каждую половину — ещё на две, и так до тех пор, пока размер куска не станет достаточно маленьким для прямого вычисления.

### **Шаг 1: Создание рекурсивной задачи**

```java
import java.util.concurrent.RecursiveTask;

public class SumTask extends RecursiveTask<Long> {
private final long[] array;
private final int start;
private final int end;
private final int threshold = 10_000; // Порог для разделения задачи
public SumTask(long[] array, int start, int end) {
    this.array = array;
    this.start = start;
    this.end = end;
}

@Override
protected Long compute() {
    int length = end - start;
    if (length <= threshold) {
        // 1. Базовый случай: задача достаточно мала, считаем напрямую
        return sum();
    } else {
        // 2. Рекурсивный случай: задача большая, делим её
        int mid = start + length / 2;

        // Создаём задачу для левой половины
        SumTask leftTask = new SumTask(array, start, mid);
        // Создаём задачу для правой половины
        SumTask rightTask = new SumTask(array, mid, end);

        // Асинхронно выполняем левую задачу
        leftTask.fork(); // ← "Вилкуем" задачу в пул

        // Выполняем правую задачу в текущем потоке
        Long rightResult = rightTask.compute(); // ← Эффективность: не создаём лишний поток

        // Ждём завершения левой задачи и получаем результат
        Long leftResult = leftTask.join(); // ← "Соединяем" результат

        return leftResult + rightResult;
    }
}

private long sum() {
    long sum = 0;
    for (int i = start; i < end; i++) {
        sum += array[i];
    }
    return sum;
	}
}
```

### **Что здесь происходит?**

1. **`compute()`**: Это главный метод. Сначала он проверяет, не слишком ли маленький у него кусок массива.
2. **Базовый случай**: Если размер массива меньше **`threshold`** (10 000 элементов), мы просто суммируем его в цикле. Это эффективнее, чем создавать новые потоки для крошечных задач.
3. **Рекурсивный случай**: Если массив большой, мы делим его пополам.
4. **`fork()`**: Этот метод асинхронно отправляет подзадачу в пул. Она может быть выполнена другим потоком.
5. **`compute()`**: Для второй подзадачи мы вызываем **`compute()`** напрямую в текущем потоке. Это распространённая оптимизация, чтобы не создавать лишнюю задачу в пуле без нужды.
6. **`join()`**: Этот метод блокирует выполнение и ждёт, пока задача, запущенная через **`fork()`**, не завершится, после чего возвращает её результат.

### **Шаг 2: Запуск задачи в ForkJoinPool**

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class ForkJoinPoolDemo {
public static void main(String[] args) throws InterruptedException {
// Создаём большой массив для демонстрации
long[] numbers = new long[100_000_000];
Random random = new Random();
for (int i = 0; i < numbers.length; i++) {
numbers[i] = random.nextInt(100); // Заполняем случайными числами
}
    // Создаём ForkJoinPool (обычно используют общий пул)
    ForkJoinPool pool = new ForkJoinPool();

    // Создаём корневую задачу
    SumTask mainTask = new SumTask(numbers, 0, numbers.length);

    long startTime = System.currentTimeMillis();

    // Запускаем задачу и ждём результата
    Long result = pool.invoke(mainTask); // ← invoke запускает и ждёт завершения

    long endTime = System.currentTimeMillis();

    System.out.println("Сумма: " + result);
    System.out.println("Время выполнения: " + (endTime - startTime) + " мс");

    pool.shutdown();
    pool.awaitTermination(1, TimeUnit.MINUTES);
	}
}

```

## **Продвинутые возможности и сравнение**

### **Сравнение ForkJoinPool и ThreadPoolExecutor**

Чтобы понять, когда использовать какой инструмент, давайте сравним их:

| **Характеристика** | **ForkJoinPool** | **ThreadPoolExecutor (обычный ExecutorService)** |
| --- | --- | --- |
| **Тип задач** | Рекурсивные, "разделяй и властвуй" | Независимые, однотипные задачи |
| **Алгоритм** | Work-stealing (кража работы) | Общая очередь задач |
| **Эффективность** | Высока для CPU-bound задач с ветвлением | Универсальна, особенно для I/O-bound задач |
| **Пример** | Обработка массивов, параллельная сортировка | Обработка веб-запросов, выполнение фоновых jobs |

### **ForkJoinPool vs. Виртуальные потоки**

Это важное различие в концепциях:

- **ForkJoinPool** нацелен на **параллелизм (Parallelism)**. Он использует все доступные ядра процессора для *одновременного* выполнения вычислений и ускорения одной большой задачи.
- **Виртуальные потоки** нацелены на **конкурентность (Concurrency)**. Они позволяют обрабатывать *миллионы* одновременно ожидающих операций (например, сетевых запросов), не блокируя при этом потоки ОС.

**Примечание:** Виртуальные потоки под капотом также могут использовать **`ForkJoinPool`** для своих нужд, но это детали реализации, скрытые от разработчика.

---

## **Практическая реализация: Параллельный поиск файлов**

Давайте создадим утилиту, которая ищет файлы с определённым текстом внутри всех файлов в заданной директории и её поддиректориях. Это идеальная задача для **`ForkJoinPool`**.

### **Шаг 1: Создание задачи поиска**

```java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.RecursiveTask;

public class FileSearchTask extends RecursiveTask<Integer> {
private final File directory;
private final String searchText;
public FileSearchTask(File directory, String searchText) {
    this.directory = directory;
    this.searchText = searchText;
}

@Override
protected Integer compute() {
    int count = 0;
    File[] files = directory.listFiles();
    if (files == null) {
        return 0;
    }

    for (File file : files) {
        if (file.isDirectory()) {
            // Рекурсивно создаём задачу для поддиректории
            FileSearchTask subTask = new FileSearchTask(file, searchText);
            subTask.fork(); // ← Отправляем в пул
            count += subTask.join(); // ← Добавляем результат
        } else if (file.getName().endsWith(".txt")) {
            // Базовый случай: ищем в текстовом файле
            count += searchInFile(file);
        }
    }
    return count;
}

private int searchInFile(File file) {
    try {
        String content = Files.readString(file.toPath());
        if (content.contains(searchText)) {
            System.out.println("Найдено в файле: " + file.getAbsolutePath());
            return 1;
        }
    } catch (IOException e) {
        System.err.println("Ошибка чтения файла: " + file.getAbsolutePath());
    }
    return 0;
	}
}
```

**Шаг 2: Запуск поиска**

```java
import java.io.File;
import java.util.concurrent.ForkJoinPool;

public class ParallelFileSearcher {
public static void main(String[] args) {
// Укажите путь к директории для поиска
File rootDir = new File("./"); // Текущая директория
String textToFind = "TODO";
    // Используем общий пул ForkJoinPool
    ForkJoinPool pool = ForkJoinPool.commonPool();

    FileSearchTask task = new FileSearchTask(rootDir, textToFind);

    System.out.println("Начинаем поиск текста '" + textToFind + "' в директории " + rootDir.getAbsolutePath());

    long startTime = System.currentTimeMillis();
    int totalMatches = pool.invoke(task);
    long endTime = System.currentTimeMillis();

    System.out.println("\\nПоиск завершён.");
    System.out.println("Всего найдено совпадений: " + totalMatches);
    System.out.println("Заняло времени: " + (endTime - startTime) + " мс");
	}
}

```

## **Заключение**

Поздравляю, вы теперь владеете мощным инструментом для параллельной обработки данных! Давайте подведём итоги:

1. **ForkJoinPool** — это специализированный **`ExecutorService`** для задач, которые можно разбить на подзадачи (принцип "разделяй и властвуй").
2. Его эффективность основана на алгоритме **work-stealing**, который не даёт потокам простаивать.
3. Для создания задач используются классы **`RecursiveTask`** (с результатом) и **`RecursiveAction`** (без результата).
4. Ключевые методы — **`fork()`** (асинхронный запуск подзадачи) и **`join()`** (получение результата).
5. **`ForkJoinPool`** идеально подходит для CPU-bound задач, таких как обработка массивов, параллельные алгоритмы и обход древовидных структур.

### **Что дальше?**

Теперь вы готовы применять **`ForkJoinPool`** для решения реальных вычислительных задач. Попробуйте следующее:

- Изучите, как работают **Parallel Streams** в Java 8+. Под капотом они как раз используют **`ForkJoinPool.commonPool()`**!
- Попробуйте реализовать параллельную версию быстрой сортировки (QuickSort) с помощью **`ForkJoinPool`**.
- Сравните производительность вашей реализации поиска файлов с однопоточной версией на большой директории.

**`ForkJoinPool`** — это не просто ещё один пул потоков, а фундаментальная концепция для высокопроизводительных вычислений на Java. Удачи в покорении многопоточности

---

## **Практическое задание**

Для закрепления материала выполните практическое задание в проекте `practice/practice-4`.

**Задача:** Реализуйте классы и методы так, чтобы все unit-тесты в `ForkJoinPoolTest.java` проходили.

**Требования:**
1. Реализуйте `SumTask`, который суммирует элементы массива рекурсивно
2. Реализуйте `MaxTask`, который находит максимальный элемент в массиве
3. Используйте правильный порог (threshold) для разделения задач
4. Правильно используйте `fork()` и `join()`

**Инструкция:**
1. Перейдите в директорию `practice/practice-4`
2. Запустите тесты: `mvn test`
3. Реализуйте недостающие классы и методы, чтобы все тесты проходили
4. Не изменяйте сами тесты!

**Подсказки:**
- Наследуйтесь от `RecursiveTask<Long>` для задач с результатом
- Используйте базовый случай (когда размер задачи меньше threshold) для прямого вычисления
- Для рекурсивного случая разделяйте задачу пополам
- Вызывайте `fork()` для одной подзадачи и `compute()` для другой (оптимизация)
- Используйте `join()` для получения результата от подзадачи