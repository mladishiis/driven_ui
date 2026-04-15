package com.example.drivenui.engine.generative_screen.widget

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация [IWidgetValueProvider] для хранения значений виджетов в памяти.
 */
@Singleton
class WidgetValueProvider @Inject constructor() : IWidgetValueProvider {

    private val widgetValues = mutableMapOf<String, MutableMap<String, Any>>()

    override fun setWidgetValue(widgetCode: String, parameter: String, value: Any) {
        val widgetParams = widgetValues.getOrPut(widgetCode) { mutableMapOf() }
        widgetParams[parameter] = value
    }

    override fun getWidgetValue(widgetCode: String, parameter: String): Any? {
        return widgetValues[widgetCode]?.get(parameter)
    }

    override fun clearWidgetValues(widgetCode: String) {
        widgetValues.remove(widgetCode)
    }

    override fun clearAll() {
        widgetValues.clear()
    }
}
