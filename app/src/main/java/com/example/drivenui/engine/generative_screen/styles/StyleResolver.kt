package com.example.drivenui.engine.generative_screen.styles

import androidx.compose.foundation.background
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
import com.example.drivenui.engine.uirender.models.CornerRadius
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.value.resolveValueExpression

/**
 * Резолвит экран: подставляет контекстные переменные в текстовые поля
 * и применяет стили (после биндингов).
 * Здесь уже можно использовать resolveValueExpression для условных выражений
 * в текстовых полях и кодах стилей, которые зависят от ${} и @{}/@@{}.
 *
 * @param screenModel Модель экрана
 * @param contextManager Менеджер контекста
 * @param styleRegistry Реестр стилей
 * @return Обновлённая модель экрана
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

/**
 * Резолвит компонент: подставляет переменные и применяет стили.
 *
 * @param component Компонент для резолва
 * @param contextManager Менеджер контекста
 * @param styleRegistry Реестр стилей
 * @return Обновлённый компонент или null
 */
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
    var cornerRadiusTop: Int? = null
    var cornerRadiusBottom: Int? = null

    layout.radiusValues.radius?.let { rawCode ->
        val resolved = resolveValueExpression(rawCode, contextManager)
        cornerRadius = resolved.toIntOrNull()
    }
    if (cornerRadius == null) {
        layout.radiusValues.radiusTop?.let { rawCode ->
            val resolved = resolveValueExpression(rawCode, contextManager)
            cornerRadiusTop = resolved.toIntOrNull()
        }
        layout.radiusValues.radiusBottom?.let { rawCode ->
            val resolved = resolveValueExpression(rawCode, contextManager)
            cornerRadiusBottom = resolved.toIntOrNull()
        }
    }

    val resolvedCornerRadius = CornerRadius(
        all = cornerRadius,
        top = cornerRadiusTop,
        bottom = cornerRadiusBottom,
    )

    layout.backgroundColorStyleCode?.let { rawCode ->
        val shouldDeferBackground =
            (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) &&
                    (rawCode.contains("\${") || rawCode.contains("{#"))

        if (!shouldDeferBackground) {
            val resolvedCode = resolveValueExpression(rawCode, contextManager)
            val color = styleRegistry.getComposeColor(resolvedCode)
            if (color != null) {
                val shape = resolvedCornerRadius.toRoundedCornerShape()
                modifier = if (shape != null) {
                    modifier.background(color = color, shape = shape)
                } else {
                    modifier.background(color)
                }
            }
        }
    }

    val resolvedVisibilityCode = layout.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: layout.visibilityCode)

    if (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) {
        return layout.copy(
            modifier = modifier,
            cornerRadius = resolvedCornerRadius,
            visibility = visibility,
            visibilityCode = resolvedVisibilityCode ?: layout.visibilityCode,
        )
    }

    val resolvedChildren = layout.children.mapNotNull {
        resolveComponent(it, contextManager, styleRegistry)
    }

    return layout.copy(
        modifier = modifier,
        cornerRadius = resolvedCornerRadius,
        children = resolvedChildren,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: layout.visibilityCode,
    )
}

private fun resolveButton(
    button: ButtonModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ButtonModel {
    val resolvedText = resolveValueExpression(button.text, contextManager)
    var cornerRadius = button.cornerRadius
    var textStyle: TextStyle = button.textStyle
    var backgroundColor: Color = button.backgroundColor

    button.radiusValues.radius?.let { rawCode ->
        val resolved = resolveValueExpression(rawCode, contextManager)
        cornerRadius = resolved.toIntOrNull()?.let { CornerRadius(all = it) } ?: cornerRadius
    }
    if (cornerRadius.all == null) {
        var top = cornerRadius.top
        var bottom = cornerRadius.bottom
        button.radiusValues.radiusTop?.let { rawCode ->
            val resolved = resolveValueExpression(rawCode, contextManager)
            top = resolved.toIntOrNull()
        }
        button.radiusValues.radiusBottom?.let { rawCode ->
            val resolved = resolveValueExpression(rawCode, contextManager)
            bottom = resolved.toIntOrNull()
        }
        cornerRadius = CornerRadius(top = top, bottom = bottom)
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
        cornerRadius = cornerRadius,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: button.visibilityCode,
    )
}

private fun resolveLabel(
    label: LabelModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): LabelModel {
    val resolvedText = resolveValueExpression(label.text, contextManager)
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
    val resolvedTitle = appBar.title?.let { resolveValueExpression(it, contextManager) }
    var textStyle: TextStyle = appBar.textStyle
    var leftIconTint: Color = appBar.leftIconTint
    var containerColor: Color = appBar.containerColor

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

    appBar.leftIconColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            leftIconTint = color
        }
    }

    appBar.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            containerColor = color
        }
    }

    val resolvedVisibilityCode = appBar.visibilityCode?.let { resolveValueExpression(it, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: appBar.visibilityCode)

    return appBar.copy(
        title = resolvedTitle,
        textStyle = textStyle,
        leftIconTint = leftIconTint,
        containerColor = containerColor,
        visibility = visibility,
        visibilityCode = resolvedVisibilityCode ?: appBar.visibilityCode
    )
}

private fun resolveInput(
    input: InputModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): InputModel {
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
