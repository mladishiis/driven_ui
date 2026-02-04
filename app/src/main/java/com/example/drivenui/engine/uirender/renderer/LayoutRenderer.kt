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

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    isRoot: Boolean = false,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
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
            onWidgetValueChange,
            applyBindingsForComponent
        )
        LayoutType.VERTICAL_FOR -> LazyColumnRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent
        )

        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent
        )
        LayoutType.HORIZONTAL_FOR -> LazyRowRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent
        )
        LayoutType.LAYER -> BoxRenderer(
            modelWithClickable,
            isRoot,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent
        )
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    isRoot: Boolean = false,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = applyBindingsForComponent?.invoke(child) ?: child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent
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
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                model = applyBindingsForComponent?.invoke(child) ?: child,
                isRoot = isRoot,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent
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
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    val forIndexName = model.forIndexName ?: return
    val maxForIndex = model.maxForIndex?.toIntOrNull() ?: return

    LazyColumn(modifier = model.modifier) {
        items(maxForIndex) { index ->
            val indexStr = index.toString()
            model.children.forEach { templateChild ->
                val expandedChild = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                val childWithBindings = applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
                ComponentRenderer(
                    model = childWithBindings,
                    isRoot = isRoot,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent
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
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    val forIndexName = model.forIndexName ?: return
    val maxForIndex = model.maxForIndex?.toIntOrNull() ?: return

    LazyRow(modifier = model.modifier) {
        items(maxForIndex) { index ->
            val indexStr = index.toString()
            model.children.forEach { templateChild ->
                val expandedChild = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                val childWithBindings = applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
                ComponentRenderer(
                    model = childWithBindings,
                    isRoot = isRoot,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent
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
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
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
                    model = applyBindingsForComponent?.invoke(child) ?: child,
                    isRoot = isRoot,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent
                )
            }
        }
    }
}

/**
 * Заменяет {#forIndexName} на конкретный индекс во всех строках компонента
 * TODO: может вынести?
 */
private fun expandComponentWithIndex(
    component: ComponentModel,
    forIndexName: String,
    index: String
): ComponentModel {
    val pattern = "{#$forIndexName}"
    fun String?.replaceIndex(): String? = this?.replace(pattern, index)
    fun String.replaceIndex(): String = this.replace(pattern, index)

    return when (component) {
        is LayoutModel -> {
            component.copy(
                children = component.children.map { child ->
                    expandComponentWithIndex(child, forIndexName, index)
                },
                backgroundColorStyleCode = component.backgroundColorStyleCode.replaceIndex()
            )
        }
        is com.example.drivenui.engine.uirender.models.LabelModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex()
            )
        }
        is com.example.drivenui.engine.uirender.models.ButtonModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                roundStyleCode = component.roundStyleCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                backgroundColorStyleCode = component.backgroundColorStyleCode.replaceIndex()
            )
        }
        is com.example.drivenui.engine.uirender.models.AppBarModel -> {
            component.copy(
                title = component.title.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex()
            )
        }
        is com.example.drivenui.engine.uirender.models.InputModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                hint = component.hint.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex()
            )
        }
        is com.example.drivenui.engine.uirender.models.ImageModel -> {
            component.copy(
                url = component.url.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex()
            )
        }
        else -> component
    }
}
