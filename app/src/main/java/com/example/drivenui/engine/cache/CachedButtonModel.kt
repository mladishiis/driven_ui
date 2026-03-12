package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель кнопки.
 *
 * @property modifierParams модификаторы
 * @property enabled активна ли кнопка
 * @property text текст
 * @property roundedCornerSize размер скругления
 * @property roundStyleCode код стиля скругления
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property backgroundColorStyleCode код стиля фона
 * @property tapActions действия при нажатии
 * @property widgetCode код виджета
 * @property alignmentStyle стиль выравнивания
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedButtonModel(
    override val modifierParams: ModifierParams,
    val enabled: Boolean,
    val text: String,
    val roundedCornerSize: Int?,
    val roundStyleCode: String?,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val backgroundColorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
