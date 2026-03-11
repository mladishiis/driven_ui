package com.example.drivenui.engine.uirender.models

/**
 * Модель UI-компонента переключателя (Switch).
 *
 * @property checked Состояние переключателя (включён/выключен)
 * @property alignmentStyle Стиль выравнивания
 * @property visibility Видимость компонента
 */
data class SwitcherModel(
    val checked: Boolean,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
) : ComponentModel