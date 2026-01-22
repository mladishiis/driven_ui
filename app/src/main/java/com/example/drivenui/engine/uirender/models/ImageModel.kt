package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier

data class ImageModel(
    val modifier: Modifier,
    val url: String?,
    val widgetCode: String,
    override val alignmentStyle: String,
) : ComponentModel