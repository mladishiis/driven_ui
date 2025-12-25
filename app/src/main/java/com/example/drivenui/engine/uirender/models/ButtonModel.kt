package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

data class ButtonModel(
    val modifier: Modifier,
    val enabled: Boolean,
    val text: String,
    val textStyle: TextStyle,
    val roundedCornerSize: Int?,
    override val alignmentStyle: String,
) : ComponentModel
