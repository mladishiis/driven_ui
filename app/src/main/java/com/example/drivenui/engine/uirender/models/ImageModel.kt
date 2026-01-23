package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

data class ImageModel(
    val modifier: Modifier,
    val url: String?,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    override val alignmentStyle: String,
) : ComponentModel