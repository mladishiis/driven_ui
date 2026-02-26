package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.PaddingValues
import com.example.drivenui.engine.uirender.models.ModifierParams
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.parser.models.Component

fun Component.getAlignmentStyle(): String =
    this.styles.find { it.code == "alignmentStyle" }?.value ?: ""

/**
 * Парсит значение visibility из строки.
 * Только "true" или "false", иначе -> true (по умолчанию видим).
 */
fun parseVisibility(resolvedValue: String?): Boolean {
    if (resolvedValue.isNullOrBlank()) return true
    return when (resolvedValue.trim().lowercase()) {
        "true" -> true
        "false" -> false
        else -> true
    }
}

fun Modifier.applyPaddingStyle(paddingStyle: PaddingValues): Modifier =
    this.padding(paddingStyle)

/**
 * Получает PaddingValues из properties компонента.
 * Читает paddingLeft, paddingTop, paddingRight, paddingBottom из properties.
 * Если какое-то свойство отсутствует, используется 0dp.
 */
fun getPaddingFromProperties(component: Component): PaddingValues {
    val p = getPaddingFromPropertiesAsInts(component)
    return PaddingValues(
        start = p[0].dp,
        top = p[1].dp,
        end = p[2].dp,
        bottom = p[3].dp
    )
}

/**
 * Создаёт ModifierParams из свойств компонента (padding + height + width).
 */
fun getModifierParamsFromComponent(component: Component): ModifierParams {
    val paddings = getPaddingFromPropertiesAsInts(component)
    val heightProperty = component.properties.find { it.code == "height" }?.resolvedValue.orEmpty()
    val widthProperty = component.properties.find { it.code == "width" }?.resolvedValue.orEmpty()
    return ModifierParams(
        paddingLeft = paddings[0],
        paddingTop = paddings[1],
        paddingRight = paddings[2],
        paddingBottom = paddings[3],
        height = heightProperty,
        width = widthProperty
    )
}

/**
 * Возвращает отступы как (left, top, right, bottom) в пикселях.
 */
fun getPaddingFromPropertiesAsInts(component: Component): IntArray {
    val paddingLeft = component.properties
        .find { it.code == "paddingLeft" }
        ?.resolvedValue
        ?.toIntOrNull() ?: 0

    val paddingTop = component.properties
        .find { it.code == "paddingTop" }
        ?.resolvedValue
        ?.toIntOrNull() ?: 0

    val paddingRight = component.properties
        .find { it.code == "paddingRight" }
        ?.resolvedValue
        ?.toIntOrNull() ?: 0

    val paddingBottom = component.properties
        .find { it.code == "paddingBottom" }
        ?.resolvedValue
        ?.toIntOrNull() ?: 0

    return intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
}