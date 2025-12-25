package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import com.example.drivenui.engine.uirender.models.SwitcherModel

@Composable
fun SwitcherRenderer(model: SwitcherModel) {
    Switch(
        checked = model.checked,
        onCheckedChange = {}
    )
}