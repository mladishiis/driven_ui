package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class ButtonModel(
    val modifier: Modifier,
    val enabled: Boolean,
    val text: String,
    val textStyle: TextStyle,
    val background: Color,
    val roundedCornerSize: Int?,
    val tapAction: List<UiAction>,
    override val alignmentStyle: String,
) : ComponentModel
