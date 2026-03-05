package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.SwitcherModel

@Composable
fun SwitcherRenderer(
    model: SwitcherModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier,
        checked = model.checked,
        onCheckedChange = {}
    )
}