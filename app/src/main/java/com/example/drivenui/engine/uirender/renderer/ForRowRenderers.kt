package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

/**
 * Есть ли в поддереве вложенный `verticalFor`/`horizontalFor` (ему по-прежнему нужен обход биндингов).
 */
internal fun subtreeContainsNestedForLayout(component: ComponentModel): Boolean {
    return when (component) {
        is LayoutModel -> {
            if (component.type == LayoutType.VERTICAL_FOR || component.type == LayoutType.HORIZONTAL_FOR) {
                true
            } else {
                component.children.any { subtreeContainsNestedForLayout(it) }
            }
        }
        else -> false
    }
}

/**
 * Одна строка `verticalFor`: разворачивает шаблон по индексу и рендерит детей.
 */
@Composable
internal fun VerticalForRowContent(
    index: Int,
    forIndexName: String,
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter?,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) {
    val indexStr = index.toString()
    Column(modifier = Modifier.fillMaxWidth().clipToBounds()) {
        model.children.forEach { templateChild ->
            val expandedChild =
                expandComponentWithIndex(templateChild, forIndexName, indexStr)
            val childWithBindings =
                applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
            val passBindingsDown =
                if (subtreeContainsNestedForLayout(childWithBindings)) {
                    applyBindingsForComponent
                } else {
                    null
                }
            ComponentRenderer(
                model = childWithBindings,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = passBindingsDown,
            )
        }
    }
}

/**
 * Одна колонка `horizontalFor`: разворачивает шаблон по индексу и рендерит детей.
 */
@Composable
internal fun HorizontalForRowContent(
    index: Int,
    forIndexName: String,
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter?,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) {
    val indexStr = index.toString()
    Row(modifier = Modifier.clipToBounds()) {
        model.children.forEach { templateChild ->
            val expandedChild =
                expandComponentWithIndex(templateChild, forIndexName, indexStr)
            val childWithBindings =
                applyBindingsForComponent?.invoke(expandedChild) ?: expandedChild
            val passBindingsDown =
                if (subtreeContainsNestedForLayout(childWithBindings)) {
                    applyBindingsForComponent
                } else {
                    null
                }
            ComponentRenderer(
                model = childWithBindings,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = passBindingsDown,
            )
        }
    }
}
