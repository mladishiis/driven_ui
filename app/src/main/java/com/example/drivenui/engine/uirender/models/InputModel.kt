package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента поля ввода.
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора
 * @property text шаблон текста из XML
 * @property hint шаблон подсказки
 * @property displayText результат резолва для отображаемого значения
 * @property displayHint результат резолва для подсказки
 * @property readOnly только чтение
 * @property widgetCode уникальный код виджета
 * @property finishTypingActions экшены при завершении ввода
 * @property tapActions экшены при нажатии
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class InputModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val text: String,
    val hint: String,
    val displayText: String? = null,
    val displayHint: String? = null,
    val readOnly: Boolean,
    val widgetCode: String,
    val finishTypingActions: List<UiAction>,
    val tapActions: List<UiAction> = emptyList(),
    override val alignment: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel