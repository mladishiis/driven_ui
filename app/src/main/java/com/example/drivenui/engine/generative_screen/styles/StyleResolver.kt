package com.example.drivenui.engine.generative_screen.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 * Резолвит экран: подстановка `${...}` и `@{...}` / `@@{...}` в поля `display*`, стили и радиусы по кодам.
 *
 * @param screenModel Модель экрана
 * @param contextManager Менеджер контекста
 * @param styleRegistry Реестр стилей
 * @param dataContext Контекст данных для `${...}`
 * @param useDarkColorPalette Брать ветку `darkTheme` цветов вместо `lightTheme` (как системная тёмная тема)
 * @return Обновлённая модель экрана
 */
fun resolveScreen(
    screenModel: ScreenModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    useDarkColorPalette: Boolean,
): ScreenModel {
    val resolvedRoot = resolveComponent(
        component = screenModel.rootComponent,
        contextManager = contextManager,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
        useDarkColorPalette = useDarkColorPalette,
    )
    return screenModel.copy(rootComponent = resolvedRoot)
}

/**
 * Резолвит компонент: подстановки и стили.
 *
 * @param dataContext Контекст данных для `${...}`
 * @param useDarkColorPalette Ветка цветов реестра: тёмная или светлая (как у устройства)
 */
fun resolveComponent(
    component: ComponentModel?,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    useDarkColorPalette: Boolean,
): ComponentModel? {
    return when (component) {
        null -> null
        is LayoutModel -> resolveLayout(
            component,
            contextManager,
            styleRegistry,
            dataContext,
            useDarkColorPalette,
        )
        is ButtonModel -> resolveButton(
            component,
            contextManager,
            styleRegistry,
            dataContext,
            useDarkColorPalette,
        )
        is LabelModel -> resolveLabel(
            component,
            contextManager,
            styleRegistry,
            dataContext,
            useDarkColorPalette,
        )
        is AppBarModel -> resolveAppBar(
            component,
            contextManager,
            styleRegistry,
            dataContext,
            useDarkColorPalette,
        )
        is InputModel -> resolveInput(component, contextManager, styleRegistry, dataContext)
        is ImageModel -> resolveImage(
            component,
            contextManager,
            styleRegistry,
            dataContext,
            useDarkColorPalette,
        )
        else -> component
    }
}

private fun resolveLayout(
    layout: LayoutModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    useDarkColorPalette: Boolean,
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

    val deferLayoutChrome =
        (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) &&
            sequenceOf(
                layout.backgroundColorStyleCode,
                layout.strokeWidth,
                layout.strokeColorStyleCode,
            ).any { raw -> raw != null && ("\${" in raw || "{#" in raw) }

    if (!deferLayoutChrome) {
        layout.backgroundColorStyleCode?.let { rawCode ->
            val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
            val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
            if (color != null) {
                val shape = resolvedCornerRadius.toRoundedCornerShape()
                modifier = if (shape != null) {
                    modifier.background(color = color, shape = shape)
                } else {
                    modifier.background(color)
                }
            }
        }
        modifier = modifier.withResolvedStroke(
            strokeWidth = layout.strokeWidth,
            strokeColorStyleCode = layout.strokeColorStyleCode,
            cornerRadius = resolvedCornerRadius,
            styleRegistry = styleRegistry,
            dataContext = dataContext,
            contextManager = contextManager,
            useDarkColorPalette = useDarkColorPalette,
        )
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
        resolveComponent(it, contextManager, styleRegistry, dataContext, useDarkColorPalette)
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
    useDarkColorPalette: Boolean,
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
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    button.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
        if (color != null) {
            backgroundColor = color
        }
    }

    val resolvedVisibilityCode =
        button.visibilityCode?.let { resolveTemplateString(it, dataContext, contextManager) }
    val visibility = parseVisibility(resolvedVisibilityCode ?: button.visibilityCode)

    val strokeAppearance = resolveStrokeAppearance(
        strokeWidth = button.stroke.width,
        strokeColorStyleCode = button.stroke.colorStyleCode,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
        contextManager = contextManager,
        useDarkColorPalette = useDarkColorPalette,
    )

    return button.copy(
        text = button.text,
        displayText = displayText,
        cornerRadius = cornerRadius,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        modifier = button.modifier,
        stroke = button.stroke.copy(
            resolvedWidthDp = strokeAppearance?.first,
            resolvedColor = strokeAppearance?.second,
        ),
        visibility = visibility,
        visibilityCode = button.visibilityCode,
    )
}

private fun resolveLabel(
    label: LabelModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    useDarkColorPalette: Boolean,
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
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
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
    useDarkColorPalette: Boolean,
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
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
        if (color != null) {
            textStyle = textStyle.copy(color = color)
        }
    }

    appBar.leftIconColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
        if (color != null) {
            leftIconTint = color
        }
    }

    appBar.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val color = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
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
    useDarkColorPalette: Boolean,
): ImageModel {
    val displayUrl = image.url?.let { resolveTemplateString(it, dataContext, contextManager) }

    var imageColor: Color = image.color
    image.colorStyleCode?.let { rawCode ->
        val resolvedCode = resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
        val resolvedColor = styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
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

/** Резолвит толщину (dp) и цвет обводки; общая основа для layout и кнопки. */
private fun resolveStrokeAppearance(
    strokeWidth: String?,
    strokeColorStyleCode: String?,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    contextManager: IContextManager,
    useDarkColorPalette: Boolean,
): Pair<Int, Color>? {
    val rawWidth = strokeWidth ?: return null
    val resolvedWidth =
        resolveTemplateString(rawWidth, dataContext, contextManager) ?: rawWidth
    val strokeDp = resolvedWidth.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val rawStrokeStyle = strokeColorStyleCode ?: return null
    val resolvedStrokeStyle =
        resolveTemplateString(rawStrokeStyle, dataContext, contextManager) ?: rawStrokeStyle
    val strokeColor = styleRegistry.getComposeColor(resolvedStrokeStyle, useDarkColorPalette) ?: return null
    return strokeDp to strokeColor
}

private fun Modifier.withResolvedStroke(
    strokeWidth: String?,
    strokeColorStyleCode: String?,
    cornerRadius: CornerRadius,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    contextManager: IContextManager,
    useDarkColorPalette: Boolean,
): Modifier {
    val stroke = resolveStrokeAppearance(
        strokeWidth = strokeWidth,
        strokeColorStyleCode = strokeColorStyleCode,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
        contextManager = contextManager,
        useDarkColorPalette = useDarkColorPalette,
    ) ?: return this
    val strokeDp = stroke.first
    val strokeColor = stroke.second
    val shape = cornerRadius.toRoundedCornerShape() ?: RoundedCornerShape(0.dp)
    return border(width = strokeDp.dp, color = strokeColor, shape = shape)
}
