package com.example.drivenui.engine.uirender.models

import androidx.compose.runtime.Stable

/**
 * Базовый интерфейс для всех UI-компонентов.
 *
 * @property alignmentStyle стиль выравнивания компонента
 * @property visibility видимость компонента (по умолчанию true)
 */
@Stable
sealed interface ComponentModel {
    val alignmentStyle: String
    val visibility: Boolean get() = true
}