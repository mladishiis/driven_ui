package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения запроса.
 *
 * @property title заголовок
 * @property code код
 * @property type тип
 * @property endpoint endpoint
 * @property propertiesCount количество свойств
 */
data class QueryItem(
    val title: String,
    val code: String,
    val type: String,
    val endpoint: String,
    val propertiesCount: Int
)
