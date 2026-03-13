package com.example.drivenui.engine.uirender.models

/**
 * Модель UI-компонента чекбокса.
 *
 * @property checked отмечен ли чекбокс
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 */
data class CheckboxModel(
    val checked: Boolean,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
) : ComponentModel