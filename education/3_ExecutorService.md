# ExecutorService, создание виртуальных потоков

## **Введение в многопоточность в Java**

Представьте, что у вас есть ресторан, в котором работает только один повар. Когда приходит много заказов, повар вынужден готовить их последовательно, и клиенты ждут долго. Что произойдет, если нанять несколько поваров? Правильно, заказы будут готовиться параллельно, и клиенты получат свои блюда быстрее.

В мире программирования потоки (threads) — это те самые "повара" в нашем "ресторане" приложений. Java предоставляет мощные инструменты для управления потоками, и один из ключевых — ExecutorService.

### **Что такое ExecutorService?**

**ExecutorService** — это высокоуровневый API для управления потоками в Java, который предоставляет удобный способ асинхронного выполнения задач. Вместо того чтобы вручную создавать потоки, вы можете просто передать задачи в ExecutorService, а он уже позаботится об их выполнении.

---

## **Основы ExecutorService**

Давайте разберёмся, как работает ExecutorService и почему он лучше, чем ручное управление потоками.

### **Преимущества использования ExecutorService:**

- ✅ Управление пулом потоков вместо создания новых каждый раз
- ✅ Контроль над количеством одновременно выполняемых задач
- ✅ Возможность получения результата выполнения задачи
- ✅ Эффективное использование системных ресурсов

### **Базовые концепции ExecutorService**

В Java есть несколько реализаций ExecutorService, но самая популярная — ThreadPoolExecutor. Давайте рассмотрим её основные компоненты:

```java
// Создание простого пула потоков
ExecutorService executor = Executors.newFixedThreadPool(10);
// Отправка задачи на выполнение
Future<String> future = executor.submit(() -> {
// Какая-то долгая операция
Thread.sleep(1000);
return "Результат выполнения";
});
// Получение результата (блокирующая операция)
String result = future.get();
```

**Что здесь происходит?**

1. Мы создали пул из 10 потоков, которые могут выполнять задачи
2. Отправили задачу в виде лямбда-выражения
3. Получили объект Future, который будет содержать результат по завершении
4. Получили результат с помощью метода get()

---

## **Практические примеры использования ExecutorService**

Давайте рассмотрим реальный пример использования ExecutorService для обработки изображений:

```java
public class ImageProcessor {

private final ExecutorService executor;

public ImageProcessor(int threadCount) {
    // Создаем пул потоков фиксированного размера
    this.executor = Executors.newFixedThreadPool(threadCount);
}

public List<ProcessedImages> processImages(List<Image> images) {
    List<Future<ProcessedImages>> futures = new ArrayList<>();

    // Отправляем каждую картинку на обработку в отдельном потоке
    for (Image image : images) {
        Future<ProcessedImages> future = executor.submit(() -> {
            // Долгая операция обработки изображения
            return processImage(image);
        });
        futures.add(future);
    }

    List<ProcessedImages> results = new ArrayList<>();
    // Собираем результаты
    for (Future<ProcessedImages> future : futures) {
        try {
            results.add(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    return results;
}

private ProcessedImages processImage(Image image) {
    // Реальная обработка изображения
    // ...
	}
}
```

### **Что здесь происходит?**

- Мы создали класс ImageProcessor с пулом потоков
- Каждое изображение обрабатывается в отдельном потоке из пула
- Результаты собираются после завершения всех задач
- Метод **`get()`** блокирует выполнение до получения результата

---

## **Продвинутые возможности: Виртуальные потоки (Virtual Threads)**

Представьте, что традиционные потоки в Java — это тяжеловесные "повара", которых нужно нанимать, обучать и обеспечивать рабочим местом. Они стоят дорого, и их количество ограничено.

А что если бы у нас были "виртуальные повара" — легковесные, недорогие, и мы могли бы создать их тысячи? Именно такую возможность представляют виртуальные потоки, появившиеся в Java 19 как preview и ставшие стандартом в Java 21.

### **Что такое виртуальные потоки?**

**Виртуальные потоки (Virtual Threads)** — это легковесные потоки, управляемые JVM, а не ОС. Они не привязаны напрямую к потокам ОС и позволяют создавать миллионы потоков без значительных затрат ресурсов.

**Преимущества виртуальных потоков:**

- ✅ Легковесность (несколько КБ памяти вместо МБ для обычного потока)
- ✅ Возможность создавать миллионы потоков
- ✅ Простота программирования в стиле "один поток на запрос"
- ✅ Эффективное использование ресурсов процессора

## **Сравнение традиционных потоков и виртуальных потоков**

Давайте сравним традиционные потоки (platform threads) и виртуальные потоки (virtual threads):

| **Характеристика** | **Platform Threads** | **Virtual Threads** |
| --- | --- | --- |
| **Создание** | Тяжеловесное, требует ресурсов ОС | Легковесное, управляется JVM |
| **Память** | ~1-2 МБ на поток | ~1-2 КБ на поток |
| **Масштабируемость** | Тысячи потоков | Миллионы потоков |
| **Блокировка I/O** | Блокирует поток ОС | Отсоединяет поток ОС и может использовать другой |

**Пример создания виртуального потока:**

```java
// Традиционный подход
Thread.startVirtualThread(() -> {
System.out.println("Выполнение в виртуальном потоке");
});

// Использование ExecutorService с виртуальными потоками
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
Future<?> future = executor.submit(() -> {
System.out.println("Задача в виртуальном потоке");
return "Результат";
});
// Получаем результат
String result = (String) future.get();
}

```

## **Практическая реализация: Создание веб-сервера с виртуальными потоками**

Давайте создадим простой веб-сервер, использующий виртуальные потоки для обработки запросов:

### **Шаг 1: Подготовка окружения**

Убедитесь, что у вас установлена Java 21 или более поздняя версия, так как виртуальные потоки стали стандартом именно в этой версии.

### **Шаг 2: Создание структуры проекта**

```java
public class VirtualThreadWebServer {
private static final int PORT = 8080;
public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(PORT);
    System.out.println("Сервер запущен на порту " + PORT);

    // Создаем ExecutorService с виртуальными потоками
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        while (true) {
            // Принимаем входящее соединение
            Socket clientSocket = serverSocket.accept();

            // Обрабатываем запрос в виртуальном потоке
            executor.submit(() -> handleRequest(clientSocket));
        }
    }
}

private static void handleRequest(Socket clientSocket) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {

        // Читаем HTTP-запрос
        String requestLine = in.readLine();
        if (requestLine == null) return;

        System.out.println("Получен запрос: " + requestLine);

        // Отправляем HTTP-ответ
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain");
        out.println();
        out.println("Ответ от сервера на виртуальных потоках!");

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	}
}

```

### **Что здесь происходит?**

- Мы создаем сервер, который слушает порт 8080
- Для каждого входящего запроса создается виртуальный поток
- Запрос обрабатывается асинхронно без блокировки основных потоков
- Благодаря виртуальным потокам мы можем обслуживать тысячи одновременных подключений

### **Шаг 3: Тестирование**

Запустите сервер и откройте в браузере **`http://localhost:8080`**. Вы должны увидеть ответ от сервера. Попробуйте открыть несколько вкладок одновременно — все запросы будут обработаны практически мгновенно.

---

## **Сравнение производительности**

Давайте проведем небольшой тест, чтобы сравнить производительность традиционных потоков и виртуальных потоков:

```java
public class ThreadPerformanceComparison {
private static final int TASK_COUNT = 10000;
public static void main(String[] args) throws InterruptedException {
    System.out.println("Тестирование производительности потоков...");

    // Тест с традиционными потоками
    long startTime = System.currentTimeMillis();
    try (ExecutorService executor = Executors.newFixedThreadPool(100)) {
        for (int i = 0; i < TASK_COUNT; i++) {
            executor.submit(() -> blockingOperation());
        }
    }
    long platformTime = System.currentTimeMillis() - startTime;

    // Тест с виртуальными потоками
    startTime = System.currentTimeMillis();
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < TASK_COUNT; i++) {
            executor.submit(() -> blockingOperation());
        }
    }
    long virtualTime = System.currentTimeMillis() - startTime;

    System.out.println("Время выполнения с традиционными потоками: " + platformTime + " мс");
    System.out.println("Время выполнения с виртуальными потоками: " + virtualTime + " мс");
    System.out.println("Ускорение: " + (double) platformTime / virtualTime + " раз");
}

private static void blockingOperation() {
    try {
        // Имитация блокирующей операции (например, запрос к БД)
        Thread.sleep(10);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
	}
}
```

При запуске этого теста вы увидите, что виртуальные потоки значительно превосходят традиционные при работе с блокирующими операциями.

---

## **Заключение**

Поздравляю, вы только что познакомились с мощными возможностями ExecutorService и виртуальных потоков в Java! Давайте подведем итоги:

1. **ExecutorService** предоставляет удобный способ управления пулом потоков и асинхронным выполнением задач.
2. **Виртуальные потоки** — это революционная технология в Java, позволяющая создавать миллионы легковесных потоков.
3. Виртуальные потоки особенно эффективны при работе с блокирующими операциями, такими как сетевые запросы или доступ к базе данных.
4. Сочетание ExecutorService и виртуальных потоков открывает новые возможности для создания высокопроизводительных приложений.

### **Что дальше?**

Теперь вы готовы создавать эффективные многопоточные приложения с использованием виртуальных потоков. Попробуйте:

- Переписать существующее приложение с использованием виртуальных потоков
- Исследовать другие структуры, такие как StructuredTaskScope для более сложных сценариев
- Изучить, как виртуальные потоки интегрируются с существующими фреймворками, такими как Spring

Мир конкурентного программирования в Java становится всё более интересным, и виртуальные потоки — это лишь начало нового этапа его развития. Удачи в ваших экспериментах!

---

## **Практическое задание**

Для закрепления материала выполните практическое задание в проекте `practice/practice-3`.

**Задача:** Реализуйте классы и методы так, чтобы все unit-тесты в `ExecutorServiceTest.java` проходили.

**Требования:**
1. Реализуйте `TaskProcessor`, который обрабатывает задачи через ExecutorService
2. Реализуйте параллельную обработку списка задач
3. Реализуйте использование виртуальных потоков (Java 21+)
4. Реализуйте правильное завершение ExecutorService
5. Правильно обрабатывайте исключения и тайм-ауты

**Инструкция:**
1. Перейдите в директорию `practice/practice-3`
2. Запустите тесты: `mvn test`
3. Реализуйте недостающие классы и методы, чтобы все тесты проходили
4. Не изменяйте сами тесты!

**Подсказки:**
- Используйте `Executors.newFixedThreadPool()` для создания пула потоков
- Для виртуальных потоков используйте `Executors.newVirtualThreadPerTaskExecutor()` (Java 21+)
- Всегда вызывайте `shutdown()` и `awaitTermination()` для корректного завершения
- Используйте `Future.get(timeout, unit)` для установки тайм-аутов