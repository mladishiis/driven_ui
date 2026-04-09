package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Сериализуемое представление экрана с замапленным деревом компонентов.
 *
 * @property id Идентификатор экрана
 * @property deeplink Deeplink экрана
 * @property onCreateActions Действия до первой отрисовки
 * @property onDestroyActions Действия при уходе с экрана назад
 * @property rootComponent Корневой компонент экрана
 */
data class CachedScreenModel(
    val id: String,
    val deeplink: String = "",
    val onCreateActions: List<UiAction>? = null,
    val onDestroyActions: List<UiAction>? = null,
    val rootComponent: CachedComponentModel?,
)
