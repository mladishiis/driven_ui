package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null
) {
    when (model.type) {
        LayoutType.VERTICAL_LAYOUT -> ColumnRenderer(model, onAction, onWidgetValueChange)
        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(model, onAction, onWidgetValueChange)
        LayoutType.LAYER -> BoxRenderer(model, onAction, onWidgetValueChange)
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(child, onAction, onWidgetValueChange)
        }
    }
}

@Composable
private fun RowRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(child, onAction, onWidgetValueChange)
        }
    }
}

@Composable
private fun BoxRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null
) {
    Box(
        modifier = model.modifier
    ) {
        model.children.forEach { child ->
            val modifier = when (child.alignmentStyle.lowercase()) {
                "aligncenter" -> Modifier
                    .align(Alignment.Center)

                "alignleft", "alignstart" -> Modifier
                    .align(Alignment.CenterStart)

                "alignright", "alignend" -> Modifier
                    .align(Alignment.CenterEnd)

                "aligntop" -> Modifier
                    .align(Alignment.TopCenter)

                "alignbottom" -> Modifier
                    .align(Alignment.BottomCenter)

                else -> Modifier
            }
            Box(modifier = modifier) {
                ComponentRenderer(child, onAction, onWidgetValueChange)
            }
        }
    }
}

