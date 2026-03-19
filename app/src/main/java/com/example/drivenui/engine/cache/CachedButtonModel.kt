package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель кнопки.
 *
 * @property modifierParams модификаторы
 * @property enabled активна ли кнопка
 * @property text текст
 * @property radius радиус всех углов в dp (приоритет над top/bottom)
 * @property radiusTop радиус верхних углов в dp
 * @property radiusBottom радиус нижних углов в dp
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property backgroundColorStyleCode код стиля фона
 * @property tapActions действия при нажатии
 * @property widgetCode код виджета
 * @property alignment выравнивание виджета
 * @property textAlignment выравнивание текста
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedButtonModel(
    override val modifierParams: ModifierParams,
    val enabled: Boolean,
    val text: String,
    val radius: String?,
    val radiusTop: String? = null,
    val radiusBottom: String? = null,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val backgroundColorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignment: String,
    val textAlignment: String = "alignCenter",
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
