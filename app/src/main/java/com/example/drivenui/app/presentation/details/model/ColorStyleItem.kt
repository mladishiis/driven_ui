package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения стиля цвета.
 *
 * @property code код стиля
 * @property lightColor цвет в светлой теме
 * @property darkColor цвет в тёмной теме
 * @property lightOpacity прозрачность в светлой теме
 * @property darkOpacity прозрачность в тёмной теме
 */
data class ColorStyleItem(
    val code: String,
    val lightColor: String,
    val darkColor: String,
    val lightOpacity: Int,
    val darkOpacity: Int
)
