package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false
) {
    LaunchedEffect(model) {
        if (isRoot) {
            val actions = model.onCreateActions
                .filterNot { it is UiAction.ExecuteQuery }
            onActions(actions)
        } else {
            onActions(model.onCreateActions)
        }
    }

    val layoutModifier = if (model.onTapActions.isNotEmpty()) {
        model.modifier.then(
            Modifier.clickable {
                onActions(model.onTapActions)
            }
        )
    } else {
        model.modifier
    }

    val modelWithClickable = model.copy(modifier = layoutModifier)

    when (model.type) {
        LayoutType.VERTICAL_LAYOUT -> ColumnRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange
        )

        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange
        )
        LayoutType.LAYER -> BoxRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange
        )
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange
            )
        }
    }
}

@Composable
private fun RowRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange
            )
        }
    }
}

@Composable
private fun BoxRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    Box(modifier = model.modifier) {
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
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange
                )
            }
        }
    }
}
