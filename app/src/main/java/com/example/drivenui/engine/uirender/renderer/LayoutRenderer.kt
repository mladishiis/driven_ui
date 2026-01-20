package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false
) {
    LaunchedEffect(model) {
        if (isRoot) {
            model.onCreateActions
                .filterNot { it is UiAction.ExecuteQuery }
                .forEach { action ->
                    onAction(action)
                }
        } else {
            model.onCreateActions.forEach { action ->
                onAction(action)
            }
        }
    }

    when (model.type) {
        LayoutType.VERTICAL_LAYOUT -> ColumnRenderer(model, onAction, onWidgetValueChange, isRoot)
        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(model, onAction, onWidgetValueChange, isRoot)
        LayoutType.LAYER -> BoxRenderer(model, onAction, onWidgetValueChange, isRoot)
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = child,
                isRoot = isRoot,
                onAction = onAction,
                onWidgetValueChange = onWidgetValueChange
            )
        }
    }
}

@Composable
private fun RowRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = child,
                isRoot = isRoot,
                onAction = onAction,
                onWidgetValueChange = onWidgetValueChange
            )
        }
    }
}

@Composable
private fun BoxRenderer(
    model: LayoutModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false
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
                ComponentRenderer(
                    model = child,
                    isRoot = isRoot,
                    onAction = onAction,
                    onWidgetValueChange = onWidgetValueChange
                )
            }
        }
    }
}

