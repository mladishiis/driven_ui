package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель поля ввода.
 *
 * @property modifierParams модификаторы
 * @property text шаблон значения
 * @property hint шаблон подсказки
 * @property displayText результат резолва значения
 * @property displayHint результат резолва подсказки
 * @property readOnly только для чтения
 * @property widgetCode код виджета
 * @property finishTypingActions действия при завершении ввода
 * @property tapActions действия при нажатии
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedInputModel(
    override val modifierParams: ModifierParams,
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
    override val visibilityCode: String? = null
) : CachedComponentModel
