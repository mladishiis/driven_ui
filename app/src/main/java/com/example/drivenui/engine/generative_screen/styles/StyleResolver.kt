package com.example.drivenui.engine.generative_screen.styles

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.resolveTemplateString
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.mappers.parseVisibility
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.CornerRadius
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

/**
 * Резолвит экран: полная подстановка `${...}` + `@{...}` / `@@{...}` в отдельные `display*` поля,
 * шаблоны в исходных полях не затираются. Стили и радиусы вычисляются по шаблонам кодов.
 *
 * @param screenModel Модель экрана
 * @param contextManager Менеджер контекста
 * @param styleRegistry Реестр стилей
 * @param dataContext Контекст данных для `${...}`
 * @return Обновлённая модель экрана
 */
fun resolveScreen(
    screenModel: ScreenModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): ScreenModel {
    val resolvedRoot = resolveComponent(
        component = screenModel.rootComponent,
        contextManager = contextManager,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
    )
    return screenModel.copy(rootComponent = resolvedRoot)
}

/**
 * Резолвит компонент: подстановки и стили.
 *
 * @param dataContext Контекст данных для `${...}`
 */
fun resolveComponent(
    component: ComponentModel?,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): ComponentModel? {
    return when (component) {
        null -> null
        is LayoutModel -> resolveLayout(component, contextManager, styleRegistry, dataContext)
        is ButtonModel -> resolveButton(component, contextManager, styleRegistry, dataContext)
        is LabelModel -> resolveLabel(component, contextManager, styleRegistry, dataContext)
        is AppBarModel -> resolveAppBar(component, contextManager, styleRegistry, dataContext)
        is InputModel -> resolveInput(component, contextManager, styleRegistry, dataContext)
        is ImageModel -> resolveImage(component, contextManager, styleRegistry, dataContext)
        else -> component
    }
}

private fun resolveLayout(
    layout: LayoutModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): LayoutModel {
    var modifier = layout.modifier

    var cornerRadius: Int? = null
    var cornerRadiusTop: Int? = null
    var cornerRadiusBottom: Int? = null

    layout.radiusValues.radius?.let { rawCode ->
        val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        cornerRadius = resolved.toIntOrNull()?.takeIf { it > 0 }
    }
    if (cornerRadius == null) {
        layout.radiusValues.radiusTop?.let { rawCode ->
            val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
            cornerRadiusTop = resolved.toIntOrNull()
        }
        layout.radiusValues.radiusBottom?.let { rawCode ->
            val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
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
            val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
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

    val resolvedVisibilityCode =
        layout.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: layout.visibilityCode)

    if (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) {
        return layout.copy(
            modifier = modifier,
            cornerRadius = resolvedCornerRadius,
            visibility = visibility,
            visibilityCode = layout.visibilityCode,
        )
    }

    val resolvedChildren = layout.children.mapNotNull {
        resolveComponent(it, contextManager, styleRegistry, dataContext)
    }

    return layout.copy(
        modifier = modifier,
        cornerRadius = resolvedCornerRadius,
        children = resolvedChildren,
        visibility = visibility,
        visibilityCode = layout.visibilityCode,
    )
}

private fun resolveButton(
    button: ButtonModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): ButtonModel {
    val displayText = resolveTemplateString(button.text, dataContext, contextManager) ?: button.text
    var cornerRadius = button.cornerRadius
    var textStyle: TextStyle = button.textStyle
    var backgroundColor: Color = button.backgroundColor

    button.radiusValues.radius?.let { rawCode ->
        val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val uniform = resolved.toIntOrNull()?.takeIf { it > 0 }
        if (uniform != null) {
            cornerRadius = CornerRadius(all = uniform)
        }
    }
    if (cornerRadius.all == null) {
        var top = cornerRadius.top
        var bottom = cornerRadius.bottom
        button.radiusValues.radiusTop?.let { rawCode ->
            val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
            top = resolved.toIntOrNull()
        }
        button.radiusValues.radiusBottom?.let { rawCode ->
            val resolved = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
            bottom = resolved.toIntOrNull()
        }
        cornerRadius = CornerRadius(top = top, bottom = bottom)
    }

    button.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    button.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    button.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            backgroundColor = color
        }
    }

    val resolvedVisibilityCode =
        button.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: button.visibilityCode)

    return button.copy(
        text = button.text,
        displayText = displayText,
        cornerRadius = cornerRadius,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        visibility = visibility,
        visibilityCode = button.visibilityCode,
    )
}

private fun resolveLabel(
    label: LabelModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): LabelModel {
    val displayText = resolveTemplateString(label.text, dataContext, contextManager) ?: label.text
    var textStyle: TextStyle = label.textStyle

    label.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    label.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    val resolvedVisibilityCode =
        label.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: label.visibilityCode)

    return label.copy(
        text = label.text,
        displayText = displayText,
        textStyle = textStyle,
        visibility = visibility,
        visibilityCode = label.visibilityCode
    )
}

private fun resolveAppBar(
    appBar: AppBarModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): AppBarModel {
    val displayTitle = appBar.title?.let { resolveTemplateString(it, dataContext, contextManager) }
    val displayIconLeftUrl =
        appBar.iconLeftUrl?.let { resolveTemplateString(it, dataContext, contextManager) }
    var textStyle: TextStyle = appBar.textStyle
    var leftIconTint: Color = appBar.leftIconTint
    var containerColor: Color = appBar.containerColor

    appBar.textStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val ts = styleRegistry.getTextStyle(resolvedCode)
        if (ts != null) {
            textStyle = textStyle.copy(
                fontSize = ts.fontSize.sp,
                fontWeight = FontWeight(ts.fontWeight),
            )
        }
    }

    appBar.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    appBar.leftIconColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            leftIconTint = color
        }
    }

    appBar.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode)
        if (color != null) {
            containerColor = color
        }
    }

    val resolvedVisibilityCode =
        appBar.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: appBar.visibilityCode)

    return appBar.copy(
        title = appBar.title,
        displayTitle = displayTitle,
        iconLeftUrl = appBar.iconLeftUrl,
        displayIconLeftUrl = displayIconLeftUrl,
        textStyle = textStyle,
        leftIconTint = leftIconTint,
        containerColor = containerColor,
        visibility = visibility,
        visibilityCode = appBar.visibilityCode
    )
}

private fun resolveInput(
    input: InputModel,
    contextManager: IContextManager,
    _styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): InputModel {
    val displayText = resolveTemplateString(input.text, dataContext, contextManager) ?: input.text
    val displayHint = resolveTemplateString(input.hint, dataContext, contextManager) ?: input.hint

    val resolvedVisibilityCode =
        input.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: input.visibilityCode)

    return input.copy(
        text = input.text,
        hint = input.hint,
        displayText = displayText,
        displayHint = displayHint,
        visibility = visibility,
        visibilityCode = input.visibilityCode
    )
}

private fun resolveImage(
    image: ImageModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
): ImageModel {
    val displayUrl = image.url?.let { resolveTemplateString(it, dataContext, contextManager) }

    var imageColor: Color = image.color
    image.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val resolvedColor = styleRegistry.getComposeColor(resolvedCode)
        if (resolvedColor != null) {
            imageColor = resolvedColor
        }
    }

    val resolvedVisibilityCode =
        image.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: image.visibilityCode)

    return image.copy(
        url = image.url,
        displayUrl = displayUrl,
        color = imageColor,
        visibility = visibility,
        visibilityCode = image.visibilityCode
    )
}
