package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LabelModel

@Composable
fun LabelRenderer(model: LabelModel,
                  onAction: (UiAction) -> Unit,) {
    Text(
        text = model.text,
        modifier = model.modifier,
        style = model.textStyle,
    )
}