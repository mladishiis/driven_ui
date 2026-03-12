package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения экранного запроса.
 *
 * @property id идентификатор
 * @property code код
 * @property screenCode код экрана
 * @property queryCode код запроса
 * @property order порядок
 */
data class ScreenQueryItem(
    val id: String,
    val code: String,
    val screenCode: String,
    val queryCode: String,
    val order: Int
)
