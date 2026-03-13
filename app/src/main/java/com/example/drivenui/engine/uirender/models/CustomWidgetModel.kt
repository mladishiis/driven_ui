package com.example.drivenui.engine.uirender.models

/**
 * Модель кастомного виджета (плейсхолдер).
 *
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 */
data class CustomWidgetModel(
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
) : ComponentModel
