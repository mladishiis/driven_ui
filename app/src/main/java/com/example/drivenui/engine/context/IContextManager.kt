package com.example.drivenui.engine.context

/**
 * Менеджер контекста переменных для микроаппов и движка.
 * Хранит переменные по microappCode и переменные уровня движка.
 */
interface IContextManager {

    /**
     * Устанавливает переменную в контекст микроаппа.
     *
     * @param microappCode Код микроаппа
     * @param variableName Имя переменной
     * @param value Значение
     */
    fun setMicroappVariable(microappCode: String, variableName: String, value: Any)

    /**
     * Получает переменную из контекста микроаппа.
     *
     * @param microappCode Код микроаппа
     * @param variableName Имя переменной
     * @return Значение или null
     */
    fun getMicroappVariable(microappCode: String, variableName: String): Any?

    /**
     * Устанавливает переменную уровня движка.
     *
     * @param variableName Имя переменной
     * @param value Значение
     */
    fun setEngineVariable(variableName: String, value: Any)

    /**
     * Получает переменную уровня движка.
     *
     * @param variableName Имя переменной
     * @return Значение или null
     */
    fun getEngineVariable(variableName: String): Any?

    /**
     * Возвращает полный контекст микроаппа.
     *
     * @param microappCode Код микроаппа
     * @return Карта переменных микроаппа
     */
    fun getMicroappContext(microappCode: String): Map<String, Any>

    /**
     * Возвращает полный контекст движка.
     *
     * @return Карта переменных движка
     */
    fun getEngineContext(): Map<String, Any>

    /**
     * Очищает контекст микроаппа.
     *
     * @param microappCode Код микроаппа
     */
    fun clearMicroappContext(microappCode: String)

    /** Очищает контекст движка. */
    fun clearEngineContext()

    /** Очищает все контексты. */
    fun clearAll()
}
