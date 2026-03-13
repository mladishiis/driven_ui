package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель кнопки.
 *
 * @property modifierParams модификаторы
 * @property enabled активна ли кнопка
 * @property text текст
 * @property roundStyleCode код стиля скругления (приоритет над top/bottom)
 * @property roundStyleTopCode код стиля скругления сверху
 * @property roundStyleBottomCode код стиля скругления снизу
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property backgroundColorStyleCode код стиля фона
 * @property tapActions действия при нажатии
 * @property widgetCode код виджета
 * @property alignmentStyle стиль выравнивания виджета
 * @property textAlignmentStyle выравнивание текста
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedButtonModel(
    override val modifierParams: ModifierParams,
    val enabled: Boolean,
    val text: String,
    val roundStyleCode: String?,
    val roundStyleTopCode: String? = null,
    val roundStyleBottomCode: String? = null,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val backgroundColorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    val textAlignmentStyle: String = "alignCenter",
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
