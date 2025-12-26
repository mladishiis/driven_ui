package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.InputModel

@Composable
fun InputRenderer(model: InputModel,
                  onAction: (UiAction) -> Unit,) {
    var text by remember { mutableStateOf(model.text) }
    BasicTextField(
        modifier = model.modifier,
        value = model.text,
        onValueChange = { },
    )
}