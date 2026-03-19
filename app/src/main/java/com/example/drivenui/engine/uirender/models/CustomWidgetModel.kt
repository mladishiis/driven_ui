package com.example.drivenui.engine.uirender.models

/**
 * Модель кастомного виджета (плейсхолдер).
 *
 * @property alignment выравнивание
 * @property visibility видимость
 */
data class CustomWidgetModel(
    override val alignment: String,
    override val visibility: Boolean = true,
) : ComponentModel
