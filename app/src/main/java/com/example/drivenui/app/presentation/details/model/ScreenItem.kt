package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения экрана с поддержкой компонентов.
 *
 * @property id идентификатор
 * @property title заголовок
 * @property code код
 * @property shortCode короткий код
 * @property deeplink deeplink
 * @property eventsCount количество событий
 * @property layoutsCount количество лэйаутов
 * @property hasComponents есть ли структура компонентов
 * @property componentCount количество компонентов
 */
data class ScreenItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val eventsCount: Int,
    val layoutsCount: Int,
    val hasComponents: Boolean = false,
    val componentCount: Int = 0
)
