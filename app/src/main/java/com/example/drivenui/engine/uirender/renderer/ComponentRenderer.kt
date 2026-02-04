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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    when (model) {
        is LayoutModel -> LayoutRenderer(
            model,
            onActions,
            onWidgetValueChange,
            isRoot,
            applyBindingsForComponent
        )
        is InputModel -> InputRenderer(model, onActions, onWidgetValueChange ?: { _, _, _ -> })
        is LabelModel -> LabelRenderer(model, onActions)
        is ImageModel -> ImageRenderer(model, onActions)
        is ButtonModel -> ButtonRenderer(model, onActions)
        is CheckboxModel -> CheckboxRenderer(model, onActions)
        is SwitcherModel -> SwitcherRenderer(model, onActions)
        is AppBarModel -> AppBarRenderer(model, onActions)
        else -> {}
    }
}