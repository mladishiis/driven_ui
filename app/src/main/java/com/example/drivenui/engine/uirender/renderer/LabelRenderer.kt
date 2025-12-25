package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.drivenui.engine.uirender.models.LabelModel

@Composable
fun LabelRenderer(model: LabelModel) {
    Text(
        text = model.text,
        modifier = model.modifier,
        style = model.textStyle,
    )
}