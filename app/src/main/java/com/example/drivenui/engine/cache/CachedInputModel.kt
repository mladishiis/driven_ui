package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель поля ввода.
 *
 * @property modifierParams модификаторы
 * @property text текст
 * @property hint подсказка
 * @property readOnly только для чтения
 * @property widgetCode код виджета
 * @property finishTypingActions действия при завершении ввода
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedInputModel(
    override val modifierParams: ModifierParams,
    val text: String,
    val hint: String,
    val readOnly: Boolean,
    val widgetCode: String,
    val finishTypingActions: List<UiAction>,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
