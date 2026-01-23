package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class LabelModel(
    val modifier: Modifier,
    val text: String,
    val textStyle: TextStyle,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    override val alignmentStyle: String,
) : ComponentModel