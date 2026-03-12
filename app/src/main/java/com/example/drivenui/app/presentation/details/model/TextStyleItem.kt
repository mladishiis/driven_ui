package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения стиля текста.
 *
 * @property code код стиля
 * @property fontFamily семейство шрифта
 * @property fontSize размер шрифта
 * @property fontWeight толщина шрифта
 */
data class TextStyleItem(
    val code: String,
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: Int
)
