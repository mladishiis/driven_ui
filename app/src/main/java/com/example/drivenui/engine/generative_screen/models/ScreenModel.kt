package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel

/**
 * Экран микроаппа для рендеринга и навигации.
 *
 * @property id Код экрана
 * @property deeplink Deeplink экрана
 * @property onCreateActions Действия до первой отрисовки; до завершения показывается лоадер
 * @property onDestroyActions Действия при уходе с экрана по навигации назад
 * @property rootComponent Дерево компонентов (корневой layout без предвыполнения onCreate)
 */
data class ScreenModel(
    val id: String,
    val deeplink: String = "",
    val onCreateActions: List<UiAction> = emptyList(),
    val onDestroyActions: List<UiAction> = emptyList(),
    val rootComponent: ComponentModel?,
)