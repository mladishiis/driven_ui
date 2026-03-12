package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения стиля отступа.
 *
 * @property code код стиля
 * @property paddingLeft отступ слева
 * @property paddingTop отступ сверху
 * @property paddingRight отступ справа
 * @property paddingBottom отступ снизу
 */
data class PaddingStyleItem(
    val code: String,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int
)
