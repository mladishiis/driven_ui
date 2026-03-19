package com.example.drivenui.engine.uirender.models

import androidx.compose.runtime.Stable

/**
 * Базовый интерфейс для всех UI-компонентов.
 *
 * @property alignment выравнивание компонента (topLeft, center, etc.)
 * @property visibility видимость компонента (по умолчанию true)
 */
@Stable
sealed interface ComponentModel {
    val alignment: String
    val visibility: Boolean get() = true
}