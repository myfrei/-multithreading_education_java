# CompletableFuture и Future в Java

## **Введение в асинхронное программирование**

Представьте, что вы шеф-повар в ресторане. Вам нужно приготовить несколько блюд одновременно, а не ждать, пока каждое блюдо будет полностью готово, прежде чем начать готовить следующее. Вы бы начали готовить суп, поставили бы его на плиту, и пока он варится, начали бы готовить салат. Именно так работает асинхронное программирование — оно позволяет выполнять несколько задач параллельно, не блокируя основной поток выполнения.

В Java для работы с асинхронными операциями у нас есть два мощных инструмента: **`Future`** и его более продвинутый наследник **`CompletableFuture`**. Давайте разберёмся, что это такое, зачем нужно и как эффективно использовать в своих проектах.

### **Что такое Future?**

**`Future`** — это интерфейс в Java, который представляет результат асинхронного вычисления. Это как заказ в ресторане: вы сделали заказ, получили чек (объект Future), и можете заниматься своими делами, пока ваш заказ готовится. Когда он будет готов, вы сможете забрать его.

**`Future`** был введен еще в Java 5 как часть пакета **`java.util.concurrent`**, и он предоставляет базовые возможности для работы с асинхронными операциями.

### **Что такое CompletableFuture?**

**`CompletableFuture`** — это усовершенствованная версия **`Future`**, появившаяся в Java 8. Если **`Future`** — это просто чек из ресторана, то **`CompletableFuture`** — это приложение для заказа еды, которое уведомляет вас, когда заказ готов, позволяет добавлять позиции в уже сделанный заказ, комбинировать несколько заказов и многое другое.

---

## **Основы Future**

Давайте начнем с основ и разберемся, как работает **`Future`**.

### **Базовые концепции Future**

**`Future`** представляет собой результат асинхронной вычислительной операции. Он предоставляет методы для проверки завершения операции, ожидания ее завершения и получения результата.

Вот основные методы интерфейса **`Future`**:

```java
public interface Future<V> {
// Отменяет выполнение задачи
boolean cancel(boolean mayInterruptIfRunning);

// Возвращает true, если задача была отменена до ее завершения
boolean isCancelled();

// Возвращает true, если задача завершилась
boolean isDone();

// Блокирует текущий поток и ждет завершения задачи, затем возвращает результат
V get() throws InterruptedException, ExecutionException;

// Блокирует текущий поток и ждет завершения задачи в течение указанного времени
V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
```

### **Пример использования Future**

Давайте рассмотрим простой пример использования **`Future`** с **`ExecutorService`**:

```java
import java.util.concurrent.*;

public class FutureExample {
public static void main(String[] args) {
// Создаем пул потоков
ExecutorService executor = Executors.newFixedThreadPool(2);
    // Создаем задачу, которая будет выполняться асинхронно
    Future<String> future = executor.submit(() -> {
        // Имитация долгой операции
        Thread.sleep(2000);
        return "Результат асинхронной операции";
    });

    // Пока задача выполняется, мы можем делать другую работу
    System.out.println("Задача запущена, делаем другую работу...");

    try {
        // Получаем результат (блокирующий вызов)
        String result = future.get();  // ← Этот вызов заблокирует поток до завершения задачи
        System.out.println("Результат: " + result);
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    } finally {
        executor.shutdown();  // ← Важно закрыть ExecutorService
    }
  }
}
```

### **Что здесь происходит?**

1. Мы создаем пул потоков с помощью **`Executors.newFixedThreadPool(2)`**.
2. С помощью метода **`submit()`** отправляем задачу на выполнение и получаем объект **`Future`**.
3. Пока задача выполняется, основной поток может продолжать свою работу.
4. Метод **`future.get()`** блокирует основной поток до завершения задачи и возвращает результат.
5. В блоке **`finally`** мы закрываем **`ExecutorService`**, чтобы освободить ресурсы.

**Важное правило:** Всегда закрывайте **`ExecutorService`**, когда он больше не нужен, чтобы избежать утечек ресурсов.

## **Ограничения Future**

Несмотря на свою полезность, **`Future`** имеет ряд ограничений:

1. **Нет простого способа комбинировать результаты нескольких Future**. Если вам нужно дождаться завершения нескольких задач и объединить их результаты, код становится сложным.
2. **Нет механизма обработки исключений** в асинхронных операциях. Исключения просто оборачиваются в **`ExecutionException`** при вызове **`get()`**.
3. **Нет возможности выполнить действие по завершении задачи** без блокировки потока.
4. **Нет возможности "прикрепить" обратный вызов (callback)**, который выполнится автоматически по завершении задачи.

Именно для решения этих проблем в Java 8 был введен **`CompletableFuture`**.

## **Основы CompletableFuture**

**`CompletableFuture`** — это реализация интерфейса **`Future`** и **`CompletionStage`**, которая предоставляет множество методов для работы с асинхронными операциями.

### **Базовые концепции CompletableFuture**

В отличие от **`Future`**, **`CompletableFuture`** позволяет:

1. Комбинировать несколько асинхронных операций.
2. Добавлять обратные вызовы, которые выполнятся при завершении операции.
3. Обрабатывать исключения в асинхронных операциях.
4. Создавать цепочки асинхронных операций.

### **Создание CompletableFuture**

Существует несколько способов создания **`CompletableFuture`**:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureCreation {

public static void main(String[] args) 
											throws ExecutionException, InterruptedException {
// 1. Создание уже завершенного CompletableFuture с результатом
CompletableFuture<String> completedFuture =
											CompletableFuture.completedFuture("Готовый результат");
    // 2. Создание CompletableFuture с помощью лямбда-выражения
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
        // Имитация долгой операции
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Результат асинхронной операции";
    });

    // 3. Создание CompletableFuture с использованием ExecutorService
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CompletableFuture<String> futureWithExecutor = CompletableFuture.supplyAsync(() -> {
        // Долгая операция
        return "Результат с использованием ExecutorService";
    }, executor);

    // Получение результатов
    System.out.println(completedFuture.get());
    System.out.println(future.get());
    System.out.println(futureWithExecutor.get());

    executor.shutdown();
	}
}

```

### **Что здесь происходит?**

1. **`CompletableFuture.completedFuture()`** создает уже завершенный **`CompletableFuture`** с указанным результатом.
2. **`CompletableFuture.supplyAsync()`** создает **`CompletableFuture`**, который асинхронно выполняет задачу, описанную в **`Supplier`**.
3. **`CompletableFuture.supplyAsync()`** с параметром **`Executor`** позволяет указать, в каком пуле потоков выполнять задачу.

**Преимущества:**

- ✅ Более гибкое создание асинхронных задач
- ✅ Возможность указать свой пул потоков
- ✅ Меньше кода по сравнению с **`Future`** и **`ExecutorService`**

---

## **Работа с CompletableFuture**

Давайте рассмотрим основные методы **`CompletableFuture`** и их применение.

### **Методы thenAccept(), thenApply(), thenRun()**

Эти методы позволяют добавить обратный вызов, который выполнится после завершения **`CompletableFuture`**:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureCallbacks {
public static void main(String[] args) 
												throws ExecutionException, InterruptedException {
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
// Имитация долгой операции
try {
		Thread.sleep(1000);
} catch (InterruptedException e) {
		e.printStackTrace();
}
	return "Результат операции";
});
    // thenApply() - преобразует результат и возвращает новый CompletableFuture
    CompletableFuture<String> transformedFuture = future.thenApply(result -> {
        return "Преобразованный результат: " + result.toUpperCase();
    });

    // thenAccept() - потребляет результат, ничего не возвращая
    CompletableFuture<Void> consumedFuture = future.thenAccept(result -> {
        System.out.println("Потребленный результат: " + result);
    });

    // thenRun() - выполняет действие после завершения, не используя результат
    CompletableFuture<Void> runFuture = future.thenRun(() -> {
        System.out.println("Операция завершена!");
    });

    // Вывод результатов
    System.out.println(transformedFuture.get());

    // Ждем завершения всех операций
    consumedFuture.get();
    runFuture.get();
	}
}

```

### **Что здесь происходит?**

1. **`thenApply()`** принимает функцию, которая преобразует результат **`CompletableFuture`** и возвращает новый **`CompletableFuture`** с преобразованным результатом.
2. **`thenAccept()`** принимает **`Consumer`**, который потребляет результат, но ничего не возвращает.
3. **`thenRun()`** принимает **`Runnable`**, который выполняется после завершения **`CompletableFuture`**, но не имеет доступа к результату.

**Примечание:** Все эти методы имеют асинхронные версии: **`thenApplyAsync()`**, **`thenAcceptAsync()`**, **`thenRunAsync()`**, которые выполняют обратный вызов в отдельном потоке.

### **Комбинирование CompletableFuture**

Одна из самых мощных возможностей **`CompletableFuture`** — это комбинирование нескольких асинхронных операций:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureComposition {
public static void main(String[] args) 
										throws ExecutionException, InterruptedException {
// thenCompose() - плоская композиция (flatMap)
CompletableFuture<String> composedFuture = 
									CompletableFuture.supplyAsync(() -> {
														return "Результат первой операции";
		}).thenCompose(firstResult -> {
// Используем результат первой операции для создания второй
									return CompletableFuture.supplyAsync(() -> {
														return firstResult + " + Результат второй операции";
		});
	});
    // thenCombine() - комбинирует два независимых CompletableFuture
    CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Результат операции 1";
    });

    CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Результат операции 2";
    });

    CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
        return result1 + " + " + result2;
    });

    // allOf() - ждет завершения всех CompletableFuture
    CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(
        CompletableFuture.supplyAsync(() -> { System.out.println("Задача 1 завершена"); return null; }),
        CompletableFuture.supplyAsync(() -> { System.out.println("Задача 2 завершена"); return null; }),
        CompletableFuture.supplyAsync(() -> { System.out.println("Задача 3 завершена"); return null; })
    );

    // anyOf() - ждет завершения хотя бы одного CompletableFuture
    CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(
        CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            return "Результат из задачи 1";
        }),
        CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "Результат из задачи 2";
        })
    );

    // Вывод результатов
    System.out.println("Результат thenCompose: " + composedFuture.get());
    System.out.println("Результат thenCombine: " + combinedFuture.get());

    allOfFuture.get();  // ← Ждем завершения всех задач
    System.out.println("Все задачи завершены!");

    System.out.println("Результат anyOf: " + anyOfFuture.get());  // ← Ждем завершения хотя бы одной задачи
	}
}
```

### **Что здесь происходит?**

1. **`thenCompose()`** позволяет плоско скомбинировать две асинхронные операции, где вторая зависит от результата первой. Это похоже на **`flatMap`** в Stream API.
2. **`thenCombine()`** комбинирует результаты двух независимых **`CompletableFuture`** после их завершения.
3. **`allOf()`** создает **`CompletableFuture`**, который завершается, когда все указанные **`CompletableFuture`** завершены.
4. **`anyOf()`** создает **`CompletableFuture`**, который завершается, когда любой из указанных **`CompletableFuture`** завершен.

### **Обработка исключений**

**`CompletableFuture`** предоставляет элегантные способы обработки исключений:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureExceptionHandling {
	public static void main(String[] args) {
// exceptionally() - обрабатывает исключение и предоставляет альтернативный результат
		CompletableFuture<String> futureWithExceptionHandling = 
																		CompletableFuture.supplyAsync(() -> {
				if (Math.random() > 0.5) {
						throw new RuntimeException("Что-то пошло не так!");
				}
			return "Успешный результат";
						}).exceptionally(ex -> {

System.out.println("Обработка исключения: " + ex.getMessage());
						return "Результат по умолчанию";
});
    // handle() - обрабатывает и результат, и исключение
    CompletableFuture<String> futureWithHandle = CompletableFuture.supplyAsync(() -> {
        if (Math.random() > 0.5) {
            throw new RuntimeException("Что-то пошло не так!");
        }
        return "Успешный результат";
    }).handle((result, ex) -> {
        if (ex != null) {
            System.out.println("Обработка исключения в handle: " + ex.getMessage());
            return "Результат по умолчанию";
        }
        return result.toUpperCase();
    });

    // whenComplete() - выполняет действие после завершения, но не изменяет результат
    CompletableFuture<String> futureWithWhenComplete = CompletableFuture.supplyAsync(() -> {
        if (Math.random() > 0.5) {
            throw new RuntimeException("Что-то пошло не так!");
        }
        return "Успешный результат";
    }).whenComplete((result, ex) -> {
        if (ex != null) {
            System.out.println("Исключение в whenComplete: " + ex.getMessage());
        } else {
            System.out.println("Результат в whenComplete: " + result);
        }
    });

    // Вывод результатов
    try {
        System.out.println("Результат с exceptionally: " + futureWithExceptionHandling.get());
        System.out.println("Результат с handle: " + futureWithHandle.get());
        System.out.println("Результат с whenComplete: " + futureWithWhenComplete.get());
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    }
	}
}
```

### **Что здесь происходит?**

1. **`exceptionally()`** обрабатывает исключение и предоставляет альтернативный результат. Этот метод похож на блок **`catch`**.
2. **`handle()`** обрабатывает и результат, и исключение. Он позволяет преобразовать результат или предоставить альтернативный в случае исключения.
3. **`whenComplete()`** выполняет действие после завершения **`CompletableFuture`**, но не изменяет результат. Он похож на блок **`finally`**.

## **Практический пример: асинхронная загрузка данных**

Давайте рассмотрим практический пример использования **`CompletableFuture`** для асинхронной загрузки данных из разных источников:

i

```java
mport java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataLoadingExample {
// Имитация загрузки данных из разных источников
	public static String loadUserData(String userId) {
			try {
					Thread.sleep(1000);  // Имитация задержки сети
			} catch (InterruptedException e) {
					e.printStackTrace();
			}
				return "Данные пользователя " + userId;
	}
public static String loadUserOrders(String userId) {
    try {
        Thread.sleep(1500);  // Имитация задержки сети
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "Заказы пользователя " + userId;
}

public static String loadUserRecommendations(String userId) {
    try {
        Thread.sleep(800);  // Имитация задержки сети
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "Рекомендации для пользователя " + userId;
}

public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(4);

    String userId = "user123";

    // Асинхронная загрузка данных из разных источников
    CompletableFuture<String> userDataFuture = CompletableFuture.supplyAsync(
        () -> loadUserData(userId), executor);

    CompletableFuture<String> userOrdersFuture = CompletableFuture.supplyAsync(
        () -> loadUserOrders(userId), executor);

    CompletableFuture<String> userRecommendationsFuture = CompletableFuture.supplyAsync(
        () -> loadUserRecommendations(userId), executor);

    // Комбинирование результатов
    CompletableFuture<String> combinedFuture = userDataFuture
        .thenCombine(userOrdersFuture, (userData, userOrders) -> {
            return userData + "\\n" + userOrders;
        })
        .thenCombine(userRecommendationsFuture, (combinedData, recommendations) -> {
            return combinedData + "\\n" + recommendations;
        });

    // Обработка исключений
    CompletableFuture<String> resultFuture = combinedFuture
        .exceptionally(ex -> {
            System.err.println("Ошибка при загрузке данных: " + ex.getMessage());
            return "Не удалось загрузить данные пользователя";
        });

    // Обработка результата
    resultFuture.thenAccept(result -> {
        System.out.println("Загруженные данные:\\n" + result);
    });

    // Ждем завершения всех операций
    try {
        resultFuture.get();
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    } finally {
        executor.shutdown();  // ← Важно закрыть ExecutorService
    }
	}
}

```

### **Что здесь происходит?**

1. Мы создаем три **`CompletableFuture`** для асинхронной загрузки данных пользователя, заказов и рекомендаций.
2. С помощью **`thenCombine()`** мы комбинируем результаты этих операций.
3. С помощью **`exceptionally()`** мы обрабатываем возможные исключения.
4. С помощью **`thenAccept()`** мы обрабатываем финальный результат.
5. В конце мы ждем завершения всех операций с помощью **`get()`** и закрываем **`ExecutorService`**.

Этот пример демонстрирует, как **`CompletableFuture`** позволяет эффективно выполнять и комбинировать несколько асинхронных операций, обрабатывать исключения и работать с результатами.

---

## **Сравнение Future и CompletableFuture**

Давайте сравним **`Future`** и **`CompletableFuture`** по основным характеристикам:

| **Характеристика** | **Future** | **CompletableFuture** |
| --- | --- | --- |
| **Создание** | Через **`ExecutorService.submit()`** | Через **`CompletableFuture.supplyAsync()`** и другие методы |
| **Получение результата** | Только через блокирующий **`get()`** | Через блокирующий **`get()`** или неблокирующие обратные вызовы |
| **Комбинирование** | Сложно, требует ручного ожидания | Легко, через **`thenCombine()`**, **`thenCompose()`** и др. |
| **Обработка исключений** | Только через **`ExecutionException`** при вызове **`get()`** | Через **`exceptionally()`**, **`handle()`** и др. |
| **Цепочки операций** | Не поддерживаются | Поддерживаются через **`thenApply()`**, **`thenAccept()`** и др. |
| **Отмена операции** | Через **`cancel()`** | Через **`cancel()`** (унаследовано от Future) |
| **Завершение вручную** | Невозможно | Через **`complete()`**, **`completeExceptionally()`** |

**Преимущества CompletableFuture:**

- ✅ Более гибкий и мощный API
- ✅ Возможность создания цепочек асинхронных операций
- ✅ Элегантная обработка исключений
- ✅ Комбинирование результатов нескольких асинхронных операций
- ✅ Возможность завершения вручную

**Преимущества Future:**

- ✅ Более простой API для базовых случаев
- ✅ Меньше возможностей для ошибок при простом использовании
- ✅ Доступен с Java 5 (в отличие от CompletableFuture, который появился в Java 8)

## **Продвинутые возможности CompletableFuture**

### **Тайм-ауты**

**`CompletableFuture`** предоставляет удобные способы установки тайм-аутов для асинхронных операций:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFutureTimeout {
			public static void main(String[] args) {
// Задача, которая выполняется дольше указанного тайм-аута
			CompletableFuture<String> future = 
										CompletableFuture.supplyAsync(() -> {
			try {
					Thread.sleep(2000);  // Имитация долгой операции
			} catch (InterruptedException e) {
					e.printStackTrace();
			}
					return "Результат долгой операции";
			});    
	  try {
        // Устанавливаем тайм-аут в 1 секунду
        String result = future.get(1, TimeUnit.SECONDS);  // ← Этот вызов вызовет TimeoutException
        System.out.println("Результат: " + result);
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    } catch (TimeoutException e) {
        System.out.println("Операция не завершилась в указанное время!");

        // Альтернативный подход с orTimeout (Java 9+)
        CompletableFuture<String> futureWithTimeout = future
            .orTimeout(1, TimeUnit.SECONDS)  // ← Устанавливаем тайм-аут
            .exceptionally(ex -> {
                if (ex instanceof java.util.concurrent.TimeoutException) {
                    return "Операция превысила тайм-аут";
                }
                return "Ошибка: " + ex.getMessage();
            });

        try {
            System.out.println("Результат с orTimeout: " + futureWithTimeout.get());
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }
	}
}
```

### **Что здесь происходит?**

1. Мы создаем **`CompletableFuture`**, который имитирует долгую операцию (2 секунды).
2. Мы пытаемся получить результат с тайм-аутом в 1 секунду с помощью **`get(1, TimeUnit.SECONDS)`**, что приводит к **`TimeoutException`**.
3. Мы показываем альтернативный подход с использованием **`orTimeout()`** (доступен с Java 9), который позволяет установить тайм-аут прямо на **`CompletableFuture`**.

**Примечание:** Метод **`orTimeout()`** доступен только начиная с Java 9. В более ранних версиях можно использовать **`completeOnTimeout()`** или ручную реализацию с помощью **`get(timeout, unit)`**.

### **Кастомный Executor**

По умолчанию **`CompletableFuture`** использует общий пул потоков **`ForkJoinPool.commonPool()`**. Однако для production-кода часто рекомендуется использовать свой пул потоков:

```java
import java.util.concurrent.*;

public class CompletableFutureCustomExecutor {

public static void main(String[] args) 
									throws ExecutionException, InterruptedException {
		// Создаем кастомный ExecutorService
		ExecutorService executor = Executors.newFixedThreadPool(4);
    // Используем кастомный Executor
    CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
        System.out.println("Задача 1 выполняется в потоке: " + Thread.currentThread().getName());
        return "Результат задачи 1";
    }, executor);

    CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
        System.out.println("Задача 2 выполняется в потоке: " + Thread.currentThread().getName());
        return "Результат задачи 2";
    }, executor);

    // Комбинируем результаты
    CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
        System.out.println("Комбинирование выполняется в потоке: " + Thread.currentThread().getName());
        return result1 + " + " + result2;
    });

    // Выводим результат
    System.out.println("Результат: " + combinedFuture.get());

    // Не забываем закрыть ExecutorService
    executor.shutdown();
	}
}
```

### **Что здесь происходит?**

1. Мы создаем свой **`ExecutorService`** с пулом из 4 потоков.
2. Мы передаем этот **`ExecutorService`** в метод **`supplyAsync()`**, чтобы задачи выполнялись в нашем пуле потоков.
3. Мы комбинируем результаты двух задач и выводим итоговый результат.
4. В конце мы закрываем **`ExecutorService`**, чтобы освободить ресурсы.

**Важное правило:** Всегда закрывайте кастомный **`ExecutorService`**, когда он больше не нужен, чтобы избежать утечек ресурсов.

---

## **Практическая реализация: асинхронный REST-клиент**

Давайте создадим простой асинхронный REST-клиент с использованием **`CompletableFuture`**:

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AsyncRestClient {

private final ExecutorService executor;

public AsyncRestClient() {
    // Создаем пул потоков для выполнения HTTP-запросов
    this.executor = Executors.newFixedThreadPool(10);
}

// Асинхронный HTTP GET-запрос
public CompletableFuture<String> get(String url) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    return reader.lines().collect(Collectors.joining());
                }
            } else {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error making HTTP request", e);
        }
    }, executor);
}

// Асинхронный HTTP GET-запрос с тайм-аутом
public CompletableFuture<String> getWithTimeout(String url, long timeout, TimeUnit unit) {
    return get(url).orTimeout(timeout, unit);
}

// Закрытие клиента
public void close() {
    executor.shutdown();
}

public static void main(String[] args) {
    AsyncRestClient client = new AsyncRestClient();

    // Пример использования
    String apiUrl = "<https://jsonplaceholder.typicode.com/posts/1>";

    // Асинхронный запрос
    CompletableFuture<String> future = client.get(apiUrl);

    // Обработка результата
    future.thenAccept(response -> {
        System.out.println("Ответ от сервера:");
        System.out.println(response);
    }).exceptionally(ex -> {
        System.err.println("Ошибка при выполнении запроса: " + ex.getMessage());
        return null;
    });

    // Запрос с тайм-аутом
    CompletableFuture<String> futureWithTimeout = client.getWithTimeout(apiUrl, 5, TimeUnit.SECONDS);

    try {
        // Ждем завершения запроса
        String response = futureWithTimeout.get();
        System.out.println("Ответ от сервера (с тайм-аутом):");
        System.out.println(response.substring(0, Math.min(100, response.length())) + "...");
    } catch (Exception e) {
        System.err.println("Ошибка: " + e.getMessage());
    } finally {
        client.close();  // ← Важно закрыть клиент
    }
	}
}
```

### **Что здесь происходит?**

1. Мы создаем класс **`AsyncRestClient`**, который инкапсулирует логику асинхронных HTTP-запросов.
2. Метод **`get()`** выполняет асинхронный HTTP GET-запрос и возвращает **`CompletableFuture`** с ответом.
3. Метод **`getWithTimeout()`** выполняет запрос с тайм-аутом.
4. В методе **`main()`** мы показываем два способа использования клиента: с обработкой результата через **`thenAccept()`** и с ожиданием результата через **`get()`**.
5. В конце мы закрываем клиент, что приводит к закрытию **`ExecutorService`**.

Этот пример демонстрирует, как **`CompletableFuture`** может быть использован для создания асинхронных API, которые легко использовать и комбинировать.

---

## **Заключение**

Поздравляю, вы только что изучили основы работы с **`Future`** и **`CompletableFuture`** в Java! Давайте подведем итоги:

### **Ключевые выводы**

1. **`Future`** представляет результат асинхронной операции, но имеет ограниченный функционал.
2. **`CompletableFuture`** — это мощное расширение **`Future`**, которое позволяет:
    - Комбинировать асинхронные операции
    - Создавать цепочки операций
    - Элегантно обрабатывать исключения
    - Работать с результатами без блокировки потока
3. **`CompletableFuture`** предоставляет множество методов для работы с асинхронными операциями:
    - **`thenApply()`**, **`thenAccept()`**, **`thenRun()`** для добавления обратных вызовов
    - **`thenCompose()`**, **`thenCombine()`** для комбинирования операций
    - **`exceptionally()`**, **`handle()`**, **`whenComplete()`** для обработки исключений
    - **`orTimeout()`**, **`completeOnTimeout()`** для работы с тайм-аутами
4. При использовании **`CompletableFuture`** в production-коде рекомендуется:
    - Использовать свой пул потоков вместо **`ForkJoinPool.commonPool()`**
    - Всегда обрабатывать возможные исключения
    - Не забывать закрывать **`ExecutorService`**, если он больше не нужен

### **Что дальше?**

Теперь вы готовы создавать эффективные асинхронные приложения с использованием **`CompletableFuture`**. Вот несколько идей для дальнейшего изучения:

1. Изучите реактивное программирование и Project Reactor или RxJava, которые предоставляют еще более мощные инструменты для работы с асинхронными операциями.
2. Попробуйте интегрировать **`CompletableFuture`** в ваши существующие проекты, особенно там, где есть долгие операции (взаимодействие с базами данных, внешними API и т.д.).
3. Изучите, как **`CompletableFuture`** используется в популярных фреймворках, таких как Spring WebFlux.

Асинхронное программирование — это мощный инструмент для создания эффективных и отзывчивых приложений. **`CompletableFuture`** делает его доступным и удобным для Java-разработчиков. Удачи в освоении этой технологии

---

## **Практическое задание**

Для закрепления материала выполните практическое задание в проекте `practice/practice-5`.

**Задача:** Реализуйте классы и методы так, чтобы все unit-тесты в `CompletableFutureTest.java` проходили.

**Требования:**
1. Реализуйте цепочки операций с использованием `thenApply`, `thenCompose`
2. Реализуйте комбинирование нескольких CompletableFuture через `thenCombine`, `allOf`
3. Реализуйте обработку ошибок через `exceptionally`, `handle`
4. Реализуйте тайм-ауты для асинхронных операций
5. Используйте `thenAccept` для обработки результатов

**Инструкция:**
1. Перейдите в директорию `practice/practice-5`
2. Запустите тесты: `mvn test`
3. Реализуйте недостающие классы и методы, чтобы все тесты проходили
4. Не изменяйте сами тесты!

**Подсказки:**
- `thenApply` преобразует результат и возвращает новый CompletableFuture
- `thenCompose` "разворачивает" вложенный CompletableFuture (аналог flatMap)
- `thenCombine` комбинирует результаты двух независимых CompletableFuture
- `allOf` ждет завершения всех задач
- `exceptionally` обрабатывает только ошибки, `handle` обрабатывает и успех, и ошибку
- Используйте `get(timeout, unit)` для установки тайм-аутов