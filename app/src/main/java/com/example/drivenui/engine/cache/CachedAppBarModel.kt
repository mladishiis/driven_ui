package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель app bar.
 *
 * @property modifierParams модификаторы
 * @property title шаблон заголовка
 * @property displayTitle результат резолва заголовка
 * @property iconLeftUrl шаблон URL иконки слева
 * @property displayIconLeftUrl результат резолва URL иконки
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета заголовка
 * @property leftIconColorStyleCode код стиля цвета левой иконки
 * @property backgroundColorStyleCode код стиля фона панели
 * @property tapActions действия при нажатии
 * @property widgetCode код виджета
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedAppBarModel(
    override val modifierParams: ModifierParams,
    val title: String?,
    val displayTitle: String? = null,
    val iconLeftUrl: String?,
    val displayIconLeftUrl: String? = null,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val leftIconColorStyleCode: String? = null,
    val backgroundColorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignment: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
