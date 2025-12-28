# Практическое задание: CompletableFuture

## Описание тестов

### testThenApply
Проверяет цепочку преобразований через `thenApply`. `AsyncTaskProcessor` должен создать цепочку, которая добавляет " World" к строке и преобразует в верхний регистр.

### testThenCompose
Проверяет плоскую композицию через `thenCompose`. `AsyncTaskProcessor` должен создать композицию двух асинхронных операций, где вторая зависит от результата первой.

### testThenCombine
Проверяет комбинирование двух независимых CompletableFuture через `thenCombine`. `AsyncTaskProcessor` должен объединить результаты двух асинхронных операций.

### testAllOf
Проверяет ожидание завершения всех задач через `allOf`. `AsyncTaskProcessor` должен дождаться завершения всех переданных задач.

### testExceptionally
Проверяет обработку ошибок через `exceptionally`. `AsyncTaskProcessor` должен обработать ошибку и вернуть альтернативное значение.

### testHandle
Проверяет обработку и успеха, и ошибки через `handle`. `AsyncTaskProcessor` должен обработать оба случая.

### testTimeout
Проверяет обработку тайм-аутов для CompletableFuture. При превышении тайм-аута должен быть выброшен `TimeoutException`.

### testThenAccept
Проверяет обработку результата через `thenAccept`. `AsyncTaskProcessor` должен обработать результат асинхронной операции.

## Классы для реализации

- **AsyncTaskProcessor**: Работа с асинхронными задачами через CompletableFuture

