package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.drivenui.engine.generative_screen.models.UiAction

data class ImageModel(
    val modifier: Modifier,
    val url: String?,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    val colorStyleCode: String? = null,
    val color: Color = Color.Unspecified,
    override val alignmentStyle: String,
) : ComponentModel