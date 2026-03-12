package com.example.drivenui.engine.generative_screen.widget

/**
 * Провайдер значений виджетов (для биндингов и сохранения состояния).
 */
interface IWidgetValueProvider {

    /**
     * Устанавливает значение параметра виджета.
     *
     * @param widgetCode код виджета
     * @param parameter имя параметра
     * @param value значение
     */
    fun setWidgetValue(widgetCode: String, parameter: String, value: Any)

    /**
     * Получает значение параметра виджета.
     *
     * @param widgetCode код виджета
     * @param parameter имя параметра
     * @return значение или null
     */
    fun getWidgetValue(widgetCode: String, parameter: String): Any?

    /**
     * Очищает все значения виджета.
     *
     * @param widgetCode код виджета
     */
    fun clearWidgetValues(widgetCode: String)

    /** Очищает все сохранённые значения виджетов. */
    fun clearAll()
}