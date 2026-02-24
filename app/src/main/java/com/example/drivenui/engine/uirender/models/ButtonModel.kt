package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class ButtonModel(
    val modifier: Modifier,
    val enabled: Boolean,
    val text: String,
    val roundedCornerSize: Int?,
    val textStyle: TextStyle = TextStyle.Default,
    val backgroundColor: Color = Color.Black,
    val roundStyleCode: String? = null,
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val backgroundColorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null
) : ComponentModel
