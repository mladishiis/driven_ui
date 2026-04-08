package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель изображения.
 *
 * @property modifierParams модификаторы
 * @property url шаблон URL
 * @property displayUrl результат резолва для загрузки
 * @property widgetCode код виджета
 * @property tapActions действия при нажатии
 * @property colorStyleCode код стиля цвета
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedImageModel(
    override val modifierParams: ModifierParams,
    val url: String?,
    val displayUrl: String? = null,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    val colorStyleCode: String?,
    override val alignment: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
