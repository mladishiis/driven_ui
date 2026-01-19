package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction

data class InputModel(
    val modifier: Modifier,
    val text: String,
    val hint: String,
    val readOnly: Boolean,
    val widgetCode: String,
    val finishTypingActions: List<UiAction>,
    override val alignmentStyle: String,
) : ComponentModel