package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.CheckboxModel

@Composable
fun CheckboxRenderer(
    model: CheckboxModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Checkbox(
        modifier = modifier,
        checked = model.checked,
        onCheckedChange = {}
    )
}