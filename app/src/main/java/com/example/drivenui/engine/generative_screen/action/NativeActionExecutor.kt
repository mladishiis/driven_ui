package com.example.drivenui.engine.generative_screen.action

/**
 * Интерфейс для выполнения экшенов нативного кода хост-приложением.
 * Реализуется в хост-приложении и передаётся в движок при инициализации.
 */
interface NativeActionExecutor {

    /**
     * Выполняет нативный экшен.
     *
     * @param actionCode Код экшена
     * @param parameters Параметры экшена
     * @return Результат выполнения
     */
    suspend fun executeAction(
        actionCode: String,
        parameters: Map<String, String>
    ): NativeActionResult
}

/**
 * Результат выполнения нативного экшена.
 */
sealed class NativeActionResult {

    /**
     * Успешное выполнение.
     *
     * @property data Данные, возвращаемые экшеном (ключ-значение для сохранения в контекст)
     */
    data class Success(val data: Map<String, Any>? = null) : NativeActionResult()

    /**
     * Ошибка выполнения.
     *
     * @property message Сообщение об ошибке
     * @property exception Исключение (если есть)
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : NativeActionResult()
}

/**
 * Регистратор исполнителя нативных экшенов для движка.
 *
 * @todo Переписать — улучшить способ регистрации зависимостей для движка
 */
object NativeActionRegistry {
    @Volatile
    private var executor: NativeActionExecutor? = null

    /**
     * Регистрирует исполнителя нативных экшенов.
     *
     * @param executor Исполнитель экшенов
     */
    fun register(executor: NativeActionExecutor) {
        this.executor = executor
    }

    /** Удаляет зарегистрированного исполнителя. */
    fun unregister() {
        this.executor = null
    }

    /**
     * Возвращает зарегистрированного исполнителя.
     *
     * @return NativeActionExecutor или null
     */
    internal fun getExecutor(): NativeActionExecutor? {
        return executor
    }
}
