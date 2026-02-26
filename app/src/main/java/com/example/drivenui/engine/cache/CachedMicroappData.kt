package com.example.drivenui.engine.cache

import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ScreenQuery

/**
 * Сериализуемое представление замапленного микроаппа.
 * Сохраняется вместо ParsedMicroappResult для быстрой загрузки без повторного маппинга.
 */
data class CachedMicroappData(
    val microappCode: String,
    val microappTitle: String,
    val allStyles: AllStyles?,
    val screens: List<CachedScreenModel>,
) {
    fun hasData(): Boolean =
        microappCode.isNotBlank() || screens.isNotEmpty() || allStyles != null
}

/**
 * Сериализуемое представление экрана с замапленным деревом компонентов.
 */
data class CachedScreenModel(
    val id: String,
    val requests: List<ScreenQuery>,
    val rootComponent: CachedComponentModel?,
)
