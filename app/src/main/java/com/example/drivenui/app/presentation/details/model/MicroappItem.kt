package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения микроаппа.
 *
 * @property id идентификатор
 * @property title заголовок
 * @property code код
 * @property shortCode короткий код
 * @property deeplink deeplink
 * @property persistents список персистентов
 */
data class MicroappItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val persistents: List<String>
)
