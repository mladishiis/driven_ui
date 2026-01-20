package com.example.drivenui.engine.uirender.renderer

import androidx.compose.runtime.Composable
import com.example.drivenui.engine.generative_screen.models.UiAction
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
fun ComponentRenderer(
    model: ComponentModel,
    isRoot: Boolean = false,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null
) {
    when (model) {
        is LayoutModel -> LayoutRenderer(model, onAction, onWidgetValueChange, isRoot)
        is InputModel -> InputRenderer(model, onAction, onWidgetValueChange ?: { _, _, _ -> })
        is LabelModel -> LabelRenderer(model, onAction)
        is ImageModel -> ImageRenderer(model, onAction)
        is ButtonModel -> ButtonRenderer(model, onAction)
        is CheckboxModel -> CheckboxRenderer(model, onAction)
        is SwitcherModel -> SwitcherRenderer(model, onAction)
        is AppBarModel -> AppBarRenderer(model, onAction)
        else -> {}
    }
}