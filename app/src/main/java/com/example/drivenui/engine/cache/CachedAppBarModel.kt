package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель app bar.
 *
 * @property modifierParams модификаторы
 * @property title заголовок
 * @property iconLeftUrl URL иконки слева
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property tapActions действия при нажатии
 * @property widgetCode код виджета
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedAppBarModel(
    override val modifierParams: ModifierParams,
    val title: String?,
    val iconLeftUrl: String?,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignment: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
