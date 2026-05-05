package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель layout-контейнера (вертикальный, горизонтальный, слои, циклы FOR).
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора (padding, размеры)
 * @property type тип layout (VERTICAL_LAYOUT, HORIZONTAL_LAYOUT, LAYER, VERTICAL_FOR, HORIZONTAL_FOR)
 * @property children дочерние компоненты
 * @property onTapActions экшены при нажатии (onTap)
 * @property backgroundColorStyleCode код стиля фона
 * @property radiusValues значения radius, radiusTop, radiusBottom из properties (вход для StyleResolver)
 * @property cornerRadius резолвенные значения радиуса (заполняет StyleResolver)
 * @property forParams параметры цикла FOR (для verticalFor/horizontalFor)
 * @property alignment выравнивание
 * @property strokeWidth толщина обводки в dp из property (может содержать шаблоны)
 * @property strokeColorStyleCode код стиля цвета обводки (styles strokeColorStyle)
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class LayoutModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val type: LayoutType,
    val children: List<ComponentModel>,
    val onTapActions: List<UiAction>,
    val backgroundColorStyleCode: String? = null,
    val strokeWidth: String? = null,
    val strokeColorStyleCode: String? = null,
    val radiusValues: RadiusValues = RadiusValues(),
    val cornerRadius: CornerRadius = CornerRadius(),
    val forParams: LayoutForParams = LayoutForParams(),
    override val alignment: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel

/** Тип layout-контейнера. */
enum class LayoutType {
    VERTICAL_LAYOUT,
    HORIZONTAL_LAYOUT,
    LAYER,
    VERTICAL_FOR,
    HORIZONTAL_FOR,
}

/**
 * Преобразует строковое представление типа layout в соответствующий enum.
 *
 * @param type строковое представление типа
 * @return соответствующий enum
 */
fun getLayoutTypeFromString(type: String) =
    when (type) {
        "vertical" -> LayoutType.VERTICAL_LAYOUT
        "horizontal" -> LayoutType.HORIZONTAL_LAYOUT
        "layers" -> LayoutType.LAYER
        "verticalFor" -> LayoutType.VERTICAL_FOR
        "horizontalFor" -> LayoutType.HORIZONTAL_FOR
        else -> LayoutType.VERTICAL_LAYOUT
    }