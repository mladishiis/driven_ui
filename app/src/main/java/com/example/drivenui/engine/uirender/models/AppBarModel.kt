package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class AppBarModel(
    val modifier: Modifier,
    val title: String?,
    val iconLeftUrl: String?,
    val textStyle: TextStyle = TextStyle.Default,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
) : ComponentModel