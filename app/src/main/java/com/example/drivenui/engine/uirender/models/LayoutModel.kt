package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

data class LayoutModel(
    val modifier: Modifier,
    val type: LayoutType,
    val children: List<ComponentModel>,
    val onCreateActions: List<UiAction>,
    val onTapActions: List<UiAction>,
    val backgroundColorStyleCode: String? = null,
    val roundStyleCode: String? = null,
    override val alignmentStyle: String,
    val forIndexName: String? = null,
    val maxForIndex: String? = null
) : ComponentModel

enum class LayoutType {
    VERTICAL_LAYOUT,
    HORIZONTAL_LAYOUT,
    LAYER,
    VERTICAL_FOR,
    HORIZONTAL_FOR,
}

fun getLayoutTypeFromString(type: String) =
    when (type) {
        "vertical" -> LayoutType.VERTICAL_LAYOUT
        "horizontal" -> LayoutType.HORIZONTAL_LAYOUT
        "layers" -> LayoutType.LAYER
        "verticalFor" -> LayoutType.VERTICAL_FOR
        "horizontalFor" -> LayoutType.HORIZONTAL_FOR
        else -> LayoutType.VERTICAL_LAYOUT
    }