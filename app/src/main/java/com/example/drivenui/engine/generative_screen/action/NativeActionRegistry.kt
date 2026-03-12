package com.example.drivenui.engine.generative_screen.action

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
