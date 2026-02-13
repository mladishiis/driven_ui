package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drivenui.parser.models.Component

fun Component.getAlignmentStyle(): String =
    this.styles.find { it.code == "alignmentStyle" }?.value ?: ""

fun Modifier.applyPaddingStyle(paddingStyle: PaddingValues): Modifier =
    this.padding(paddingStyle)

/**
 * Получает PaddingValues из properties компонента.
 * Читает paddingLeft, paddingTop, paddingRight, paddingBottom из properties.
 * Если какое-то свойство отсутствует, используется 0dp.
 */
fun getPaddingFromProperties(component: Component): PaddingValues {
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

    return PaddingValues(
        start = paddingLeft.dp,
        top = paddingTop.dp,
        end = paddingRight.dp,
        bottom = paddingBottom.dp
    )
}