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

/**
 * Результат парсинга микроаппа с новой структурой
 */
data class ParsedMicroappNew(
    val microapp: Microapp? = null,
    val styles: AllStyles? = null,
    val events: AllEvents? = null,
    val eventActions: AllEventActions? = null,
    val screens: List<ParsedScreen> = emptyList(),
    val queries: List<Query> = emptyList(),
    val screenQueries: List<ScreenQuery> = emptyList(),
    val widgets: List<Widget> = emptyList(),
    val layouts: List<Layout> = emptyList()
)