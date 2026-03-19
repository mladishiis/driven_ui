package com.example.drivenui.engine.uirender.models

/**
 * Модель UI-компонента переключателя (Switch).
 *
 * @property checked Состояние переключателя (включён/выключен)
 * @property alignment Выравнивание
 * @property visibility Видимость компонента
 */
data class SwitcherModel(
    val checked: Boolean,
    override val alignment: String,
    override val visibility: Boolean = true,
) : ComponentModel