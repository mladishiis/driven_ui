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
 * @property onTapActions действия при нажатии (onTap)
 * @property backgroundColorStyleCode код стиля фона
 * @property radius радиус всех углов в dp (приоритет над top/bottom)
 * @property radiusTop радиус верхних углов в dp
 * @property radiusBottom радиус нижних углов в dp
 * @property alignment выравнивание
 * @property forIndexName имя переменной индекса для шаблонов
 * @property maxForIndex максимальное значение индекса
 * @property visibility видимость
 * @property visibilityCode код видимости
 */
data class CachedLayoutModel(
    override val modifierParams: ModifierParams,
    val type: LayoutType,
    val children: List<CachedComponentModel>,
    val onTapActions: List<UiAction>,
    val backgroundColorStyleCode: String?,
    val radius: String?,
    val radiusTop: String? = null,
    val radiusBottom: String? = null,
    override val alignment: String,
    val forIndexName: String?,
    val maxForIndex: String?,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
