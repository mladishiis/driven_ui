package com.example.drivenui.parser.models

/**
 * Упрощенный экран с компонентами
 */
data class ParsedScreen(
    val title: String,
    val screenCode: String,
    val screenShortCode: String,
    val deeplink: String,
    val rootComponent: Component? = null
)