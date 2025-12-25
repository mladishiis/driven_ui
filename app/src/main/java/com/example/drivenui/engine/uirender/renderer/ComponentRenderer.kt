package com.example.drivenui.engine.uirender.renderer

import androidx.compose.runtime.Composable
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.CheckboxModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.SwitcherModel

@Composable
fun ComponentRenderer(model: ComponentModel) {
    when (model) {
        is LayoutModel -> LayoutRenderer(model)
        is InputModel -> InputRenderer(model)
        is LabelModel -> LabelRenderer(model)
        is ImageModel -> ImageRenderer(model)
        is ButtonModel -> ButtonRenderer(model)
        is CheckboxModel -> CheckboxRenderer(model)
        is SwitcherModel -> SwitcherRenderer(model)
        is AppBarModel -> AppBarRenderer(model)
        else -> {}
    }
}