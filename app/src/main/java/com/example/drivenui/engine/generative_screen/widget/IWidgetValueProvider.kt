package com.example.drivenui.engine.generative_screen.widget

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface IWidgetValueProvider {

    fun setWidgetValue(widgetCode: String, parameter: String, value: Any)

    fun getWidgetValue(widgetCode: String, parameter: String): Any?

    fun clearWidgetValues(widgetCode: String)

    fun clearAll()
}

@Singleton
class WidgetValueProvider @Inject constructor() : IWidgetValueProvider {

    private val widgetValues = mutableMapOf<String, MutableMap<String, Any>>()

    override fun setWidgetValue(widgetCode: String, parameter: String, value: Any) {
        val widgetParams = widgetValues.getOrPut(widgetCode) { mutableMapOf() }
        widgetParams[parameter] = value
        Log.d("WidgetValueProvider", "Set widget value: $widgetCode.$parameter = $value")
    }

    override fun getWidgetValue(widgetCode: String, parameter: String): Any? {
        return widgetValues[widgetCode]?.get(parameter)
    }

    override fun clearWidgetValues(widgetCode: String) {
        widgetValues.remove(widgetCode)
        Log.d("WidgetValueProvider", "Cleared values for widget: $widgetCode")
    }

    override fun clearAll() {
        widgetValues.clear()
        Log.d("WidgetValueProvider", "Cleared all widget values")
    }
}