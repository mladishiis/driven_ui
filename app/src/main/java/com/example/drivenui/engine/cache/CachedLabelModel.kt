package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель лейбла.
 *
 * @property modifierParams модификаторы
 * @property text шаблон текста из разметки
 * @property displayText результат резолва для отрисовки
 * @property widgetCode код виджета
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property tapActions действия при нажатии
 * @property alignment выравнивание виджета
 * @property textAlignment выравнивание текста
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedLabelModel(
    override val modifierParams: ModifierParams,
    val text: String,
    val displayText: String? = null,
    val widgetCode: String,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val tapActions: List<UiAction>,
    override val alignment: String,
    val textAlignment: String = "alignLeft",
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
