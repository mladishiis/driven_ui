package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier

data class ImageModel(
    val modifier: Modifier,
    val url: String?, override val alignmentStyle: String,
) : ComponentModel