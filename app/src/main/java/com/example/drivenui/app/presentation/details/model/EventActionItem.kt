package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения действия события.
 *
 * @property id идентификатор
 * @property title заголовок
 * @property code код
 * @property order порядок
 * @property propertiesCount количество свойств
 */
data class EventActionItem(
    val id: String,
    val title: String,
    val code: String,
    val order: Int,
    val propertiesCount: Int
)
