package com.example.drivenui.parser.models

/**
 * Модель микроаппа
 *
 * @property title Человекочитаемое название микроаппа
 * @property code Уникальный код микроаппа (например, "microappVTB")
 * @property shortCode Сокращенный код для deeplinks (например, "maVTB")
 * @property deeplink Базовый deeplink микроаппа
 * @property persistents Список переменных, сохраняемых между сессиями
 */
data class Microapp(
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val persistents: List<String>
)