package com.example.drivenui.engine.cache

import com.example.drivenui.engine.parser.models.ScreenQuery

/**
 * Сериализуемое представление экрана с замапленным деревом компонентов.
 *
 * @property id Идентификатор экрана
 * @property deeplink Deeplink экрана
 * @property requests Список запросов экрана
 * @property rootComponent Корневой компонент экрана
 */
data class CachedScreenModel(
    val id: String,
    val deeplink: String = "",
    val requests: List<ScreenQuery>,
    val rootComponent: CachedComponentModel?,
)
