package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

private fun alignmentForStyle(alignmentStyle: String): Alignment =
    when (alignmentStyle.trim().lowercase()) {
        "aligncenter" -> Alignment.Center
        "alignleft", "alignstart" -> Alignment.CenterStart
        "alignright", "alignend" -> Alignment.CenterEnd
        "aligntop" -> Alignment.TopCenter
        "alignbottom" -> Alignment.BottomCenter
        else -> Alignment.Center
    }

private fun alignmentHorizontalForStyle(alignmentStyle: String): Alignment.Horizontal =
    when (alignmentStyle.trim().lowercase()) {
        "alignleft", "alignstart" -> Alignment.Start
        "alignright", "alignend" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

private fun alignmentVerticalForStyle(alignmentStyle: String): Alignment.Vertical =
    when (alignmentStyle.trim().lowercase()) {
        "aligntop" -> Alignment.Top
        "alignbottom" -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
    isRoot: Boolean = false,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    LaunchedEffect(Unit) {
        if (isRoot) {
            val actions = model.onCreateActions
                .filterNot { it is UiAction.ExecuteQuery }
            onActions(actions)
        } else {
            onActions(model.onCreateActions)
        }
    }

    val baseModifier = modifier
        .then(model.modifierParams.applyParams(Modifier))
        .then(model.modifier)
    val layoutModifier = if (model.onTapActions.isNotEmpty()) {
        baseModifier.then(
            Modifier.clickable {
                onActions(model.onTapActions)
            }
        )
    } else {
        baseModifier
    }

    val modelWithClickable = model.copy(modifier = layoutModifier)

    when (model.type) {
        LayoutType.VERTICAL_LAYOUT -> ColumnRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.VERTICAL_FOR -> LazyColumnRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.HORIZONTAL_FOR -> LazyRowRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.LAYER -> BoxRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(alignmentHorizontalForStyle(child.alignmentStyle)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
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
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(alignmentVerticalForStyle(child.alignmentStyle)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
        }
    }
}

@Composable
private fun LazyColumnRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    val forIndexName = model.forParams.forIndexName ?: return
    val maxForIndex = model.forParams.maxForIndex?.toIntOrNull() ?: return

    LazyColumn(modifier = model.modifier) {
        items(maxForIndex) { index ->
            val indexStr = index.toString()
            model.children.forEach { templateChild ->
                val expandedChild = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                val childWithBindings =
                    applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
                ComponentRenderer(
                    model = childWithBindings,
                    isRoot = isRoot,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent,
                )
            }
        }
    }
}

@Composable
private fun LazyRowRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    val forIndexName = model.forParams.forIndexName ?: return
    val maxForIndex = model.forParams.maxForIndex?.toIntOrNull() ?: return

    LazyRow(modifier = model.modifier) {
        items(maxForIndex) { index ->
            val indexStr = index.toString()
            model.children.forEach { templateChild ->
                val expandedChild = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                val childWithBindings =
                    applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
                ComponentRenderer(
                    model = childWithBindings,
                    isRoot = isRoot,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent,
                )
            }
        }
    }
}

@Composable
private fun BoxRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Box(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(alignmentForStyle(child.alignmentStyle)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
        }
    }
}

