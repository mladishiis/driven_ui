package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения виджета.
 *
 * @property id идентификатор
 * @property title заголовок
 * @property code код
 * @property type тип
 * @property propertiesCount количество свойств
 * @property stylesCount количество стилей
 * @property eventsCount количество событий
 */
data class WidgetItem(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val propertiesCount: Int,
    val stylesCount: Int,
    val eventsCount: Int
)
