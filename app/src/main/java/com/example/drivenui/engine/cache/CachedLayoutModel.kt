package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Кэшированная модель лэйаута.
 *
 * @property modifierParams модификаторы
 * @property type тип лэйаута
 * @property children дочерние компоненты
 * @property onCreateActions действия при создании
 * @property onTapActions действия при нажатии
 * @property backgroundColorStyleCode код стиля фона
 * @property roundStyleCode код стиля скругления
 * @property alignmentStyle стиль выравнивания
 * @property forIndexName имя переменной индекса для шаблонов
 * @property maxForIndex максимальное значение индекса
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedLayoutModel(
    override val modifierParams: ModifierParams,
    val type: LayoutType,
    val children: List<CachedComponentModel>,
    val onCreateActions: List<UiAction>,
    val onTapActions: List<UiAction>,
    val backgroundColorStyleCode: String?,
    val roundStyleCode: String?,
    override val alignmentStyle: String,
    val forIndexName: String?,
    val maxForIndex: String?,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
