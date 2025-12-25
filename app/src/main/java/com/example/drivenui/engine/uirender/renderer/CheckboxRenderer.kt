package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import com.example.drivenui.engine.uirender.models.CheckboxModel

@Composable
fun CheckboxRenderer(model: CheckboxModel) {
    Checkbox(
        checked = model.checked,
        onCheckedChange = {}
    )
}