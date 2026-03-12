package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения лэйаута.
 *
 * @property id идентификатор
 * @property title заголовок
 * @property code код
 * @property propertiesCount количество свойств
 */
data class LayoutItem(
    val id: String,
    val title: String,
    val code: String,
    val propertiesCount: Int
)
