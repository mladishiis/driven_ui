package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

data class LayoutModel(
    val modifier: Modifier,
    val type: LayoutType,
    val children: List<ComponentModel>,
    val onCreateActions: List<UiAction>,
    override val alignmentStyle: String
) : ComponentModel

enum class LayoutType {
    VERTICAL_LAYOUT,
    HORIZONTAL_LAYOUT,
    LAYER,
}

fun getLayoutTypeFromString(type: String) =
    when (type) {
        "vertical" -> LayoutType.VERTICAL_LAYOUT
        "horizontal" -> LayoutType.HORIZONTAL_LAYOUT
        "layers" -> LayoutType.LAYER
        else -> LayoutType.VERTICAL_LAYOUT
    }