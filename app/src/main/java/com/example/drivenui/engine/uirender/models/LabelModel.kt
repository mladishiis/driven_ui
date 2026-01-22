package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

data class LabelModel(
    val modifier: Modifier,
    val text: String,
    val textStyle: TextStyle,
    val widgetCode: String,
    override val alignmentStyle: String,
) : ComponentModel