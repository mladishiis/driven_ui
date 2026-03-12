package com.example.drivenui.engine.generative_screen.action

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
