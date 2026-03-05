package com.example.drivenui.engine.uirender.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
    isRoot: Boolean = false,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    if (!model.visibility) return
    when (model) {
        is LayoutModel -> LayoutRenderer(
            model = model,
            onActions = onActions,
            modifier = modifier,
            isRoot = isRoot,
            onWidgetValueChange = onWidgetValueChange,
            applyBindingsForComponent = applyBindingsForComponent,
        )
        is InputModel -> InputRenderer(
            model = model,
            onActions = onActions,
            onWidgetValueChange = onWidgetValueChange ?: { _, _, _ -> },
            modifier = modifier,
        )
        is LabelModel -> LabelRenderer(model = model, onActions = onActions, modifier = modifier)
        is ImageModel -> ImageRenderer(model = model, onActions = onActions, modifier = modifier)
        is ButtonModel -> ButtonRenderer(model = model, onActions = onActions, modifier = modifier)
        is CheckboxModel -> CheckboxRenderer(model = model, onActions = onActions, modifier = modifier)
        is SwitcherModel -> SwitcherRenderer(model = model, onActions = onActions, modifier = modifier)
        is AppBarModel -> AppBarRenderer(model = model, onActions = onActions, modifier = modifier)
        else -> {}
    }
}