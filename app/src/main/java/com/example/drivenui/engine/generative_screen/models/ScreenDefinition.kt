package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel

/** 
 * Дерево [rootComponent] содержит сырые строки (`${…}`, `{#i}`) и коды стилей.
 * Не предназначено для прямой отрисовки в Compose — сначала строится [ScreenPresentation].
 *
 * @property id Код экрана
 * @property deeplink Deeplink экрана
 * @property onCreateActions Действия до первой отрисовки
 * @property onDestroyActions Действия при уходе с экрана
 * @property rootComponent Корневой layout из JSON (после маппинга, без резолва шаблонов)
 */
data class ScreenDefinition(
    val id: String,
    val deeplink: String = "",
    val onCreateActions: List<UiAction> = emptyList(),
    val onDestroyActions: List<UiAction> = emptyList(),
    val rootComponent: ComponentModel? = null,
)
