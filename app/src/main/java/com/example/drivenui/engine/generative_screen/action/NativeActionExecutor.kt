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
