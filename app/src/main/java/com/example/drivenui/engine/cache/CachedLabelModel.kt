package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель лейбла.
 *
 * @property modifierParams модификаторы
 * @property text текст
 * @property widgetCode код виджета
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property tapActions действия при нажатии
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedLabelModel(
    override val modifierParams: ModifierParams,
    val text: String,
    val widgetCode: String,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val tapActions: List<UiAction>,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
