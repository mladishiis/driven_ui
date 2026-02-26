package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class LabelModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val text: String,
    val widgetCode: String,
    val textStyle: TextStyle = TextStyle.Default,
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel