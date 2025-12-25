package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drivenui.parser.models.Component

fun Component.getAlignmentStyle(): String =
    this.styles.find { it.code == "alignmentStyle" }?.value ?: ""

fun Component.getRoundStyle(styleRegistry: ComposeStyleRegistry): Int? {
    val roundStyle = this.styles.find { it.code == "roundStyle" }?.value
    if (roundStyle == null) return null
    return styleRegistry.getRoundStyle(roundStyle)?.radiusValue
}

fun Modifier.applyPaddingStyle(paddingStyle: PaddingValues): Modifier =
    this.padding(paddingStyle)

fun getPaddingFromCode(
    paddingCode: String,
    styleRegistry: ComposeStyleRegistry
): PaddingValues {
    return styleRegistry.getPaddingStyle(paddingCode)?.let {
        PaddingValues(
            top = it.paddingTop.dp,
            start = it.paddingRight.dp,
            bottom = it.paddingBottom.dp,
            end = it.paddingLeft.dp,
        )
    } ?: PaddingValues(0.dp)
}