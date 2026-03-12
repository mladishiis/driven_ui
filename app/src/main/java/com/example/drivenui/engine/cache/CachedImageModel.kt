package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель изображения.
 *
 * @property modifierParams модификаторы
 * @property url URL изображения
 * @property widgetCode код виджета
 * @property tapActions действия при нажатии
 * @property colorStyleCode код стиля цвета
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedImageModel(
    override val modifierParams: ModifierParams,
    val url: String?,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    val colorStyleCode: String?,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
