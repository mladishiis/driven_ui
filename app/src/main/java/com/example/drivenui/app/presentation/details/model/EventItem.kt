package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения события.
 *
 * @property title заголовок
 * @property code код
 * @property actionsCount количество действий
 */
data class EventItem(
    val title: String,
    val code: String,
    val actionsCount: Int
)
