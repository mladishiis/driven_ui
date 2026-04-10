package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.parseBoxAlignment
import com.example.drivenui.engine.uirender.parseColumnAlignment
import com.example.drivenui.engine.uirender.parseRowAlignment

/**
 * Сколько строк FOR и выше используем виртуализацию ([RecyclerView] + [androidx.compose.ui.platform.ComposeView]
 * в ячейке). Ниже порога — [Column]/[Row] + scroll: все ячейки в одной композиции, проще и без
 * пересоздания слотов при скролле.
 *
 * При необходимости подстройте значение (например 150–400) под UX и железо.
 */
private const val FOR_USE_LAZY_LIST_FROM_COUNT = 250

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
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
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.VERTICAL_FOR -> LazyColumnRenderer(
            modelWithClickable,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(
            modelWithClickable,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.HORIZONTAL_FOR -> LazyRowRenderer(
            modelWithClickable,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )

        LayoutType.LAYER -> BoxRenderer(
            modelWithClickable,
            onActions,
            onWidgetValueChange,
            applyBindingsForComponent,
        )
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(parseColumnAlignment(child.alignment)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(parseRowAlignment(child.alignment)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    val forIndexName = model.forParams.forIndexName ?: return
    val maxForIndex = model.forParams.resolvedMaxForIndex?.toIntOrNull()
        ?: model.forParams.maxForIndex?.toIntOrNull()
        ?: return

    val useLazyList = maxForIndex >= FOR_USE_LAZY_LIST_FROM_COUNT
    if (useLazyList) {
        VerticalForRecyclerList(
            modifier = model.modifier.clipToBounds(),
            forIndexName = forIndexName,
            maxForIndex = maxForIndex,
            model = model,
            onActions = onActions,
            onWidgetValueChange = onWidgetValueChange,
            applyBindingsForComponent = applyBindingsForComponent,
        )
    } else {
        val scroll = rememberScrollState()
        Column(modifier = model.modifier.verticalScroll(scroll).clipToBounds()) {
            repeat(maxForIndex) { index ->
                VerticalForRowContent(
                    index = index,
                    forIndexName = forIndexName,
                    model = model,
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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    val forIndexName = model.forParams.forIndexName ?: return
    val maxForIndex = model.forParams.resolvedMaxForIndex?.toIntOrNull()
        ?: model.forParams.maxForIndex?.toIntOrNull()
        ?: return

    val useLazyList = maxForIndex >= FOR_USE_LAZY_LIST_FROM_COUNT
    if (useLazyList) {
        HorizontalForRecyclerList(
            modifier = model.modifier.clipToBounds(),
            forIndexName = forIndexName,
            maxForIndex = maxForIndex,
            model = model,
            onActions = onActions,
            onWidgetValueChange = onWidgetValueChange,
            applyBindingsForComponent = applyBindingsForComponent,
        )
    } else {
        val scroll = rememberScrollState()
        Row(modifier = model.modifier.horizontalScroll(scroll).clipToBounds()) {
            repeat(maxForIndex) { index ->
                HorizontalForRowContent(
                    index = index,
                    forIndexName = forIndexName,
                    model = model,
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
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
) {
    Box(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier.align(parseBoxAlignment(child.alignment)),
                model = applyBindingsForComponent?.invoke(child) ?: child,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
        }
    }
}

