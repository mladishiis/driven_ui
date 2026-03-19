package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента поля ввода.
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора
 * @property text текущий текст
 * @property hint подсказка (placeholder)
 * @property readOnly только чтение
 * @property widgetCode уникальный код виджета
 * @property finishTypingActions экшены при завершении ввода
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class InputModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val text: String,
    val hint: String,
    val readOnly: Boolean,
    val widgetCode: String,
    val finishTypingActions: List<UiAction>,
    override val alignment: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel