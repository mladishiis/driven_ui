package com.example.drivenui.engine.generative_screen.action

/**
 * Интерфейс для выполнения экшенов нативного кода хост-приложением.
 * Его надо будет реализовать в нем и потом прокинуть движку.
 */
interface NativeActionExecutor {
    suspend fun executeAction(
        actionCode: String,
        parameters: Map<String, String>
    ): NativeActionResult
}

sealed class NativeActionResult {
    data class Success(val data: Map<String, Any>? = null) : NativeActionResult()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : NativeActionResult()
}

/**
 * Пока не придумала как лучшим образом регистрировать зависимости для движка
 * Для тестирования создала этот регистратор отбработчика экшенов
 *
 * TODO: Переписать
 */
object NativeActionRegistry {
    @Volatile
    private var executor: NativeActionExecutor? = null

    fun register(executor: NativeActionExecutor) {
        this.executor = executor
    }

    fun unregister() {
        this.executor = null
    }

    internal fun getExecutor(): NativeActionExecutor? {
        return executor
    }
}
