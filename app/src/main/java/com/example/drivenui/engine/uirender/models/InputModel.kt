package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier


data class InputModel(
    val modifier: Modifier,
    val text: String,
    val hint: String,
    val readOnly: Boolean, override val alignmentStyle: String,
) : ComponentModel