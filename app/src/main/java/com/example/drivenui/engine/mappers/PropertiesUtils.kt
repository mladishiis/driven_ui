package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Получает выравнивание из property с кодом alignment.
 *
 * @receiver Компонент (Layout, Widget)
 * @return Значение property или пустую строку
 */
fun Component.getAlignment(): String =
    this.properties["alignment"] ?: ""

/**
 * Получает выравнивание текста из property с кодом textAlignment (для Button, Label).
 *
 * @receiver Компонент (Button, Label)
 * @return Значение property или пустую строку
 */
fun Component.getTextAlignment(): String =
    this.properties["textAlignment"] ?: ""

/**
 * Преобразует строку textAlignment в Compose TextAlign.
 *
 * @param value alignLeft, alignCenter, alignRight; или left, center, right, justify
 * @return Выравнивание текста (Start, Center, End, Justify)
 */
fun parseTextAlign(value: String): TextAlign = when (value.trim().lowercase()) {
    "aligncenter", "align_center", "center" -> TextAlign.Center
    "alignright", "align_right", "alignend", "align_end", "right" -> TextAlign.End
    "alignjustify", "align_justify", "justify" -> TextAlign.Justify
    else -> TextAlign.Start // alignLeft, left, alignStart, default
}

/**
 * Парсит значение visibility из строки.
 *
 * @param resolvedValue "true", "false" или null/пусто — по умолчанию true
 * @return true если видим, false если скрыт
 */
fun parseVisibility(resolvedValue: String?): Boolean {
    if (resolvedValue.isNullOrBlank()) return true
    return when (resolvedValue.trim().lowercase()) {
        "true" -> true
        "false" -> false
        else -> true
    }
}

/**
 * Возвращает отступы компонента в пикселях.
 *
 * @param component Компонент с properties paddingLeft, paddingTop, paddingRight, paddingBottom
 * @return IntArray [left, top, right, bottom]
 */
fun getPaddingFromPropertiesAsInts(component: Component): IntArray {
    val paddingLeft = component.properties["paddingLeft"]?.toIntOrNull() ?: 0
    val paddingTop = component.properties["paddingTop"]?.toIntOrNull() ?: 0
    val paddingRight = component.properties["paddingRight"]?.toIntOrNull() ?: 0
    val paddingBottom = component.properties["paddingBottom"]?.toIntOrNull() ?: 0

    return intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

/**
 * Получает PaddingValues из properties компонента.
 *
 * @param component Компонент с properties paddingLeft, paddingTop, paddingRight, paddingBottom
 * @return Отступы в dp (start, top, end, bottom) для Modifier.padding
 */
fun getPaddingFromProperties(component: Component): PaddingValues {
    val p = getPaddingFromPropertiesAsInts(component)
    return PaddingValues(
        start = p[0].dp,
        top = p[1].dp,
        end = p[2].dp,
        bottom = p[3].dp,
    )
}

/**
 * Создаёт ModifierParams из свойств компонента (padding, height, width).
 *
 * @param component Компонент с properties padding*, height, width
 * @return Параметры для Modifier (отступы, height/width: fillMax, wrapContent или px) — для applyParams
 */
fun getModifierParamsFromComponent(component: Component): ModifierParams {
    val paddings = getPaddingFromPropertiesAsInts(component)
    val heightProperty = component.properties["height"] ?: ""
    val widthProperty = component.properties["width"] ?: ""
    return ModifierParams(
        paddingLeft = paddings[0],
        paddingTop = paddings[1],
        paddingRight = paddings[2],
        paddingBottom = paddings[3],
        height = heightProperty,
        width = widthProperty,
    )
}
