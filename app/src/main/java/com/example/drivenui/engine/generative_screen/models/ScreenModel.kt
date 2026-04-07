package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.parser.models.ScreenQuery

/**
 * Экран микроаппа для рендеринга и навигации.
 *
 * @property id Код экрана
 * @property deeplink Deeplink экрана
 * @property requests Запросы экрана (mock и биндинги)
 * @property onCreateActions Действия до первой отрисовки; до завершения показывается лоадер
 * @property onDestroyActions Действия при уходе с экрана по навигации назад
 * @property rootComponent Дерево компонентов (корневой layout без предвыполнения onCreate)
 */
data class ScreenModel(
    val id: String,
    val deeplink: String = "",
    val requests: List<ScreenQuery>,
    val onCreateActions: List<UiAction> = emptyList(),
    val onDestroyActions: List<UiAction> = emptyList(),
    val rootComponent: ComponentModel?,
)