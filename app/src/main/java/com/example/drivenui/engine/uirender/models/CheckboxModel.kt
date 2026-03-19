package com.example.drivenui.engine.uirender.models

/**
 * Модель UI-компонента чекбокса.
 *
 * @property checked отмечен ли чекбокс
 * @property alignment выравнивание
 * @property visibility видимость
 */
data class CheckboxModel(
    val checked: Boolean,
    override val alignment: String,
    override val visibility: Boolean = true,
) : ComponentModel