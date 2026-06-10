package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.ModifierParams
import com.example.drivenui.engine.uirender.models.sduiModifierParams
import com.example.drivenui.engine.uirender.utils.WidgetValueSetter
import com.example.drivenui.engine.uirender.utils.parseBoxAlignment
import com.example.drivenui.engine.uirender.utils.parseColumnAlignment
import com.example.drivenui.engine.uirender.utils.parseRowAlignment

private val LocalInsideVerticalScroll = compositionLocalOf { false }

@Composable
fun LayoutRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    val currentIsDark = LocalIsDarkTheme.current
    val useDarkColorPalette = isSystemInDarkTheme()
    val styleRegistry = LocalStyleRegistry.current
    val resolvedBgColor = model.backgroundColorStyleCode?.let {
        styleRegistry?.getComposeColor(it, useDarkColorPalette)
    }
    val effectiveIsDark = when {
        resolvedBgColor != null && resolvedBgColor.luminance() > 0.5f -> false
        resolvedBgColor != null && resolvedBgColor.luminance() <= 0.5f -> true
        else -> currentIsDark
    }

    val layoutModifier = modifier
        .then(model.resolveSizeModifier())
        .then(model.modifier)
        .withOnTapClickable(model.onTapActions, onActions)

    val modelWithResolvedModifier = model.copy(modifier = layoutModifier)

    val contentColor = resolvedBgColor?.let {
        if (it.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    }
    CompositionLocalProvider(
        LocalIsDarkTheme provides effectiveIsDark,
        LocalContentColor provides (contentColor ?: LocalContentColor.current),
    ) {
        when (model.type) {
            LayoutType.VERTICAL_LAYOUT,
            LayoutType.VERTICAL_FOR,
            -> ColumnRenderer(
                model = modelWithResolvedModifier,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
            )

            LayoutType.HORIZONTAL_LAYOUT,
            LayoutType.HORIZONTAL_FOR,
            -> RowRenderer(
                modelWithResolvedModifier,
                onActions,
                onWidgetValueChange,
            )

            LayoutType.LAYER -> BoxRenderer(
                modelWithResolvedModifier,
                onActions,
                onWidgetValueChange,
            )
        }
    }
}

@Composable
private fun ColumnRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    val parentIsScrollable = LocalInsideVerticalScroll.current
    val applyScroll = model.modifierParams.scrollable && !parentIsScrollable
    val useLazyScroll = applyScroll && model.modifierParams.lazyScroll

    if (useLazyScroll) {
        CompositionLocalProvider(LocalInsideVerticalScroll provides true) {
            LazyColumn(modifier = model.modifier) {
                items(model.children.size) { index ->
                    val child = model.children[index]
                    ComponentRenderer(
                        modifier = Modifier.fillMaxWidth(),
                        model = child,
                        onActions = onActions,
                        onWidgetValueChange = onWidgetValueChange,
                    )
                }
            }
        }
        return
    }

    val columnModifier = if (applyScroll) {
        model.modifier.verticalScroll(rememberScrollState())
    } else {
        model.modifier
    }
    CompositionLocalProvider(LocalInsideVerticalScroll provides (parentIsScrollable || applyScroll)) {
        Column(modifier = columnModifier) {
            model.children.forEach { child ->
                ComponentRenderer(
                    modifier = columnChildPercentModifier(
                        Modifier.align(parseColumnAlignment(child.alignment)),
                        child.sduiModifierParams(),
                    ),
                    model = child,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                )
            }
        }
    }
}

@Composable
private fun RowRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    if (model.modifierParams.scrollable && model.modifierParams.lazyScroll) {
        LazyRow(modifier = model.modifier) {
            items(model.children.size) { index ->
                val child = model.children[index]
                ComponentRenderer(
                    model = child,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                )
            }
        }
        return
    }

    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = rowChildPercentModifier(
                    Modifier.align(parseRowAlignment(child.alignment)),
                    child.sduiModifierParams(),
                ),
                model = child,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
            )
        }
    }
}

@Composable
private fun BoxRenderer(
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter? = null,
) {
    Box(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(
                modifier = Modifier
                    .align(parseBoxAlignment(child.alignment))
                    .thenBoxChildPercent(child.sduiModifierParams()),
                model = child,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
            )
        }
    }
}

/**
 * Row: width% → [RowScope.weight] (доля от всей ширины Row, не от остатка после siblings).
 * height% → [fillMaxHeight]: у всех children одна и та же maxHeight Row.
 */
private fun RowScope.rowChildPercentModifier(
    base: Modifier,
    params: ModifierParams,
): Modifier {
    var modifier = base
    params.widthFillFraction?.let { modifier = modifier.weight(it) }
    params.heightFillFraction?.let { modifier = modifier.fillMaxHeight(it) }
    return modifier
}

/**
 * Column: height% → [ColumnScope.weight]; width% → [fillMaxWidth] (полная ширина Column).
 */
private fun ColumnScope.columnChildPercentModifier(
    base: Modifier,
    params: ModifierParams,
): Modifier {
    var modifier = base
    params.heightFillFraction?.let { modifier = modifier.weight(it) }
    params.widthFillFraction?.let { modifier = modifier.fillMaxWidth(it) }
    return modifier
}

/**
 * Box: каждый child меряется с полными constraints контейнера — fillMax*(fraction) корректен.
 */
private fun Modifier.thenBoxChildPercent(params: ModifierParams): Modifier {
    var modifier = this
    params.widthFillFraction?.let { modifier = modifier.fillMaxWidth(it) }
    params.heightFillFraction?.let { modifier = modifier.fillMaxHeight(it) }
    return modifier
}

/**
 * Размеры layout: для скроллируемого вертикального контейнера без [fillMaxHeight],
 * иначе [fillMaxHeight] конфликтует с [verticalScroll] / [LazyColumn] в [ColumnRenderer].
 */
private fun LayoutModel.resolveSizeModifier(): Modifier {
    val isVerticalScrollable = modifierParams.scrollable &&
        (type == LayoutType.VERTICAL_LAYOUT || type == LayoutType.VERTICAL_FOR)
    return if (isVerticalScrollable) {
        modifierParams.applyParamsExcludingHeight(Modifier)
    } else {
        modifierParams.applyParams(Modifier)
    }
}

private fun Modifier.withOnTapClickable(
    onTapActions: List<UiAction>,
    onActions: (List<UiAction>) -> Unit,
): Modifier =
    if (onTapActions.isNotEmpty()) {
        then(Modifier.clickable { onActions(onTapActions) })
    } else {
        this
    }
