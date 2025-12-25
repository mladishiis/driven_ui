package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

data class AppBarModel(
    val modifier: Modifier,
    val title: String?,
    val iconLeftUrl: String?,
    val textStyle: TextStyle = TextStyle.Default,
    override val alignmentStyle: String,
) : ComponentModel