package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

data class AppBarModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val title: String?,
    val iconLeftUrl: String?,
    val textStyle: TextStyle = TextStyle.Default,
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null
) : ComponentModel