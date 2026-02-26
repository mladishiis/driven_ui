package com.example.drivenui.engine.generative_screen.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.mappers.parseVisibility
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.value.resolveValueExpression

/**
 * Резолвит экран: подставляет контекстные переменные в текстовые поля
 * и применяет стили (после биндингов).
 * Здесь уже можно использовать resolveValueExpression для условных выражений
 * в текстовых полях и кодах стилей, которые зависят от ${} и @{}/@@{}.
 */
fun resolveScreen(
    screenModel: ScreenModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ScreenModel {
    val resolvedRoot = resolveComponent(
        component = screenModel.rootComponent,
        contextManager = contextManager,
        styleRegistry = styleRegistry
    )
    return screenModel.copy(rootComponent = resolvedRoot)
}

fun resolveComponent(
    component: ComponentModel?,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ComponentModel? {
    return when (component) {
        null -> null
        is LayoutModel -> resolveLayout(component, contextManager, styleRegistry)
        is ButtonModel -> resolveButton(component, contextManager, styleRegistry)
        is LabelModel -> resolveLabel(component, contextManager, styleRegistry)
        is AppBarModel -> resolveAppBar(component, contextManager, styleRegistry)
        is InputModel -> resolveInput(component, contextManager, styleRegistry)
        is ImageModel -> resolveImage(component, contextManager, styleRegistry)
        else -> component
    }
}

private fun resolveLayout(
    layout: LayoutModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): LayoutModel {
    var modifier = layout.modifier

    var cornerRadius: Int? = null
    layout.roundStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val roundStyle = styleRegistry.getRoundStyle(resolvedCode)
        cornerRadius = roundStyle?.radiusValue
    }

    layout.backgroundColorStyleCode?.let { rawCode ->
        val shouldDeferBackground =
            (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) &&
                    (rawCode.contains("\${") || rawCode.contains("{#"))

        if (!shouldDeferBackground) {
            val resolvedCode = resolveValueExpression(rawCode, contextManager)
            val color = styleRegistry.getComposeColor(resolvedCode)
            if (color != null) {
                val cr = cornerRadius
                modifier = if (cr != null) {
                    modifier.background(
                        color = color,
                        shape = RoundedCornerShape(cr.dp),
                    )
                } else {
                    modifier.background(color)
                }
            }
        }
    }

    // Резолвим visibility (условные выражения *if...*then...*else и @{}/@@{})
    val resolvedVisibilityCode = layout.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: layout.visibilityCode)

    // Для FOR‑лейаутов не трогаем детей на этом этапе
    if (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) {
        return layout.copy(modifier = modifier, visibility = visibility, visibilityCode = resolvedVisibilityCode ?: layout.visibilityCode)
    }

    val resolvedChildren = layout.children.mapNotNull {
        resolveComponent(it, contextManager, styleRegistry)
    }

    return layout.copy(
        modifier = modifier,
        children = resolvedChildren,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: layout.visibilityCode
    )
}

private fun resolveButton(
    button: ButtonModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ButtonModel {
    // Резолвим текстовые поля
    val resolvedText = resolveValueExpression(button.text, contextManager)

    // Применяем стили
    var roundedCornerSize = button.roundedCornerSize
    var textStyle: TextStyle = button.textStyle
    var backgroundColor: Color = button.backgroundColor

    button.roundStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val roundStyle = styleRegistry.getRoundStyle(resolvedCode)
        roundedCornerSize = roundStyle?.radiusValue
    }

    button.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    button.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    button.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            backgroundColor = color
        }
    }

    val resolvedVisibilityCode = button.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: button.visibilityCode)

    return button.copy(
        text = resolvedText,
        roundedCornerSize = roundedCornerSize,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: button.visibilityCode
    )
}

private fun resolveLabel(
    label: LabelModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): LabelModel {
    // Резолвим текстовые поля
    val resolvedText = resolveValueExpression(label.text, contextManager)

    // Применяем стили
    var textStyle: TextStyle = label.textStyle

    label.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    label.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    val resolvedVisibilityCode = label.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: label.visibilityCode)

    return label.copy(
        text = resolvedText,
        textStyle = textStyle,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: label.visibilityCode
    )
}

private fun resolveAppBar(
    appBar: AppBarModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): AppBarModel {
    // Резолвим текстовые поля
    val resolvedTitle = appBar.title?.let { resolveValueExpression(it, contextManager) }

    // Применяем стили
    var textStyle: TextStyle = appBar.textStyle

    appBar.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    appBar.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    val resolvedVisibilityCode = appBar.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: appBar.visibilityCode)

    return appBar.copy(
        title = resolvedTitle,
        textStyle = textStyle,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: appBar.visibilityCode
    )
}

private fun resolveInput(
    input: InputModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): InputModel {
    // Резолвим текстовые поля
    val resolvedText = resolveValueExpression(input.text, contextManager)
    val resolvedHint = resolveValueExpression(input.hint, contextManager)

    val resolvedVisibilityCode = input.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: input.visibilityCode)

    return input.copy(
        text = resolvedText,
        hint = resolvedHint,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: input.visibilityCode
    )
}

private fun resolveImage(
    image: ImageModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ImageModel {
    // Резолвим текстовые поля
    val resolvedUrl = image.url?.let { resolveValueExpression(it, contextManager) }

    var imageColor: Color = image.color
    image.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val resolvedColor = styleRegistry.getComposeColor(resolvedCode)
        if (resolvedColor != null) {
            imageColor = resolvedColor
        }
    }

    val resolvedVisibilityCode = image.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: image.visibilityCode)

    return image.copy(
        url = resolvedUrl,
        color = imageColor,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: image.visibilityCode
    )
}
