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
import com.example.drivenui.engine.uirender.models.RadiusValues

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
    val context = StyleResolveContext(
        contextManager = contextManager,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
        useDarkColorPalette = useDarkColorPalette,
    )
    val resolvedRoot = resolveComponent(
        component = screenModel.rootComponent,
        context = context,
    )
    return screenModel.copy(rootComponent = resolvedRoot)
}

/**
 * Резолвит компонент: подстановки и стили.
 *
 * @param component Модель компонента для резолва
 * @param contextManager Менеджер контекста
 * @param styleRegistry Реестр стилей
 * @param dataContext Контекст данных для `${...}`
 * @param useDarkColorPalette Ветка цветов реестра: тёмная или светлая (как у устройства)
 * @return Обновлённая модель компонента или null
 */
fun resolveComponent(
    component: ComponentModel?,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry,
    dataContext: DataContext,
    useDarkColorPalette: Boolean,
): ComponentModel? {
    val context = StyleResolveContext(
        contextManager = contextManager,
        styleRegistry = styleRegistry,
        dataContext = dataContext,
        useDarkColorPalette = useDarkColorPalette,
    )
    return resolveComponent(component, context)
}

private fun resolveComponent(
    component: ComponentModel?,
    context: StyleResolveContext,
): ComponentModel? {
    return when (component) {
        null -> null
        is LayoutModel -> resolveLayout(component, context)
        is ButtonModel -> resolveButton(component, context)
        is LabelModel -> resolveLabel(component, context)
        is AppBarModel -> resolveAppBar(component, context)
        is InputModel -> resolveInput(component, context)
        is ImageModel -> resolveImage(component, context)
        else -> component
    }
}

private data class StyleResolveContext(
    val contextManager: IContextManager,
    val styleRegistry: ComposeStyleRegistry,
    val dataContext: DataContext,
    val useDarkColorPalette: Boolean,
)

private fun StyleResolveContext.resolveTemplate(rawCode: String?): String? {
    if (rawCode == null) return null
    return resolveTemplateString(rawCode, dataContext, contextManager) ?: rawCode
}

private fun StyleResolveContext.resolveColorStyle(rawCode: String?): Color? {
    val resolvedCode = resolveTemplate(rawCode) ?: return null
    return styleRegistry.getComposeColor(resolvedCode, useDarkColorPalette)
}

private fun StyleResolveContext.resolveVisibility(visibilityCode: String?): Boolean {
    val resolvedVisibilityCode = visibilityCode?.let { resolveTemplate(it) }
    return parseVisibility(resolvedVisibilityCode ?: visibilityCode)
}

private fun StyleResolveContext.resolveTextStyle(
    baseTextStyle: TextStyle,
    textStyleCode: String?,
): TextStyle {
    val resolvedCode = resolveTemplate(textStyleCode) ?: return baseTextStyle
    val resolvedTextStyle = styleRegistry.getTextStyle(resolvedCode) ?: return baseTextStyle
    return baseTextStyle.copy(
        fontSize = resolvedTextStyle.fontSize.sp,
        fontWeight = FontWeight(resolvedTextStyle.fontWeight),
    )
}

private fun StyleResolveContext.resolveCornerRadius(
    radiusValues: RadiusValues,
    baseCornerRadius: CornerRadius = CornerRadius(),
): CornerRadius {
    var cornerRadius = baseCornerRadius.all
    var cornerRadiusTop = baseCornerRadius.top
    var cornerRadiusBottom = baseCornerRadius.bottom

    radiusValues.radius?.let { rawCode ->
        val resolvedCornerRadius = resolveTemplate(rawCode)
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
        if (resolvedCornerRadius != null) {
            cornerRadius = resolvedCornerRadius
        }
    }
    if (cornerRadius == null) {
        radiusValues.radiusTop?.let { rawCode ->
            cornerRadiusTop = resolveTemplate(rawCode)?.toIntOrNull()
        }
        radiusValues.radiusBottom?.let { rawCode ->
            cornerRadiusBottom = resolveTemplate(rawCode)?.toIntOrNull()
        }
    }

    return CornerRadius(
        all = cornerRadius,
        top = cornerRadiusTop,
        bottom = cornerRadiusBottom,
    )
}

private fun resolveLayout(
    layout: LayoutModel,
    context: StyleResolveContext,
): LayoutModel {
    var modifier = layout.modifier

    val resolvedCornerRadius = context.resolveCornerRadius(layout.radiusValues)

    if (!layout.shouldDeferLayoutChrome()) {
        modifier = modifier
            .withResolvedBackground(
                backgroundColorStyleCode = layout.backgroundColorStyleCode,
                cornerRadius = resolvedCornerRadius,
                context = context,
            )
            .withResolvedStroke(
                strokeWidth = layout.strokeWidth,
                strokeColorStyleCode = layout.strokeColorStyleCode,
                cornerRadius = resolvedCornerRadius,
                context = context,
            )
    }

    val visibility = context.resolveVisibility(layout.visibilityCode)

    if (layout.isForLayout()) {
        val resolvedChildren = layout.children.map { child ->
            resolveForChildIfPossible(
                child,
                context,
            )
        }

        return layout.copy(
            modifier = modifier,
            cornerRadius = resolvedCornerRadius,
            children = resolvedChildren,
            visibility = visibility,
            visibilityCode = layout.visibilityCode,
        )
    }

    val resolvedChildren = layout.children.mapNotNull {
        resolveComponent(it, context)
    }

    return layout.copy(
        modifier = modifier,
        cornerRadius = resolvedCornerRadius,
        children = resolvedChildren,
        visibility = visibility,
        visibilityCode = layout.visibilityCode,
    )
}

private fun LayoutModel.isForLayout(): Boolean {
    return type == LayoutType.VERTICAL_FOR || type == LayoutType.HORIZONTAL_FOR
}

private fun LayoutModel.shouldDeferLayoutChrome(): Boolean {
    return isForLayout() &&
        sequenceOf(
            backgroundColorStyleCode,
            strokeWidth,
            strokeColorStyleCode,
        ).any { it.hasForDeferredTemplate() }
}

private fun resolveForChildIfPossible(
    component: ComponentModel,
    context: StyleResolveContext,
): ComponentModel {
    if (component.hasForDeferredTemplate()) return component
    return resolveComponent(
        component,
        context,
    ) ?: component
}

private fun ComponentModel.hasForDeferredTemplate(): Boolean {
    return when (this) {
        is LayoutModel -> sequenceOf(
            backgroundColorStyleCode,
            strokeWidth,
            strokeColorStyleCode,
            radiusValues.radius,
            radiusValues.radiusTop,
            radiusValues.radiusBottom,
            forParams.maxForIndex,
            forParams.resolvedMaxForIndex,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() } ||
            children.any { it.hasForDeferredTemplate() }
        is ButtonModel -> sequenceOf(
            text,
            displayText,
            widgetCode,
            textStyleCode,
            colorStyleCode,
            backgroundColorStyleCode,
            stroke.width,
            stroke.colorStyleCode,
            textAlignment,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() }
        is LabelModel -> sequenceOf(
            text,
            displayText,
            widgetCode,
            textStyleCode,
            colorStyleCode,
            textAlignment,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() }
        is AppBarModel -> sequenceOf(
            title,
            displayTitle,
            iconLeftUrl,
            displayIconLeftUrl,
            widgetCode,
            textStyleCode,
            colorStyleCode,
            leftIconColorStyleCode,
            backgroundColorStyleCode,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() }
        is InputModel -> sequenceOf(
            text,
            hint,
            displayText,
            displayHint,
            widgetCode,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() }
        is ImageModel -> sequenceOf(
            url,
            displayUrl,
            widgetCode,
            colorStyleCode,
            visibilityCode,
        ).any { it.hasForDeferredTemplate() }
        else -> false
    }
}

private fun String?.hasForDeferredTemplate(): Boolean {
    return this != null && ("\${" in this || "{#" in this)
}

private fun resolveButton(
    button: ButtonModel,
    context: StyleResolveContext,
): ButtonModel {
    val displayText = context.resolveTemplate(button.text) ?: button.text
    val cornerRadius = context.resolveCornerRadius(button.radiusValues, button.cornerRadius)
    var textStyle = context.resolveTextStyle(button.textStyle, button.textStyleCode)
    var backgroundColor = button.backgroundColor

    context.resolveColorStyle(button.colorStyleCode)?.let { color ->
        textStyle = textStyle.copy(color = color)
    }
    context.resolveColorStyle(button.backgroundColorStyleCode)?.let { color ->
        backgroundColor = color
    }

    val visibility = context.resolveVisibility(button.visibilityCode)

    val strokeAppearance = context.resolveStrokeAppearance(
        strokeWidth = button.stroke.width,
        strokeColorStyleCode = button.stroke.colorStyleCode,
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
    context: StyleResolveContext,
): LabelModel {
    val displayText = context.resolveTemplate(label.text) ?: label.text
    var textStyle = context.resolveTextStyle(label.textStyle, label.textStyleCode)

    context.resolveColorStyle(label.colorStyleCode)?.let { color ->
        textStyle = textStyle.copy(color = color)
    }

    val visibility = context.resolveVisibility(label.visibilityCode)

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
    context: StyleResolveContext,
): AppBarModel {
    val displayTitle = context.resolveTemplate(appBar.title)
    val displayIconLeftUrl = context.resolveTemplate(appBar.iconLeftUrl)
    var textStyle = context.resolveTextStyle(appBar.textStyle, appBar.textStyleCode)
    var leftIconTint: Color = appBar.leftIconTint
    var containerColor: Color = appBar.containerColor

    context.resolveColorStyle(appBar.colorStyleCode)?.let { color ->
        textStyle = textStyle.copy(color = color)
    }
    context.resolveColorStyle(appBar.leftIconColorStyleCode)?.let { color ->
        leftIconTint = color
    }
    context.resolveColorStyle(appBar.backgroundColorStyleCode)?.let { color ->
        containerColor = color
    }

    val visibility = context.resolveVisibility(appBar.visibilityCode)

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
    context: StyleResolveContext,
): InputModel {
    val displayText = context.resolveTemplate(input.text) ?: input.text
    val displayHint = context.resolveTemplate(input.hint) ?: input.hint

    val visibility = context.resolveVisibility(input.visibilityCode)

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
    context: StyleResolveContext,
): ImageModel {
    val displayUrl = context.resolveTemplate(image.url)

    var imageColor: Color = image.color
    context.resolveColorStyle(image.colorStyleCode)?.let { color ->
        imageColor = color
    }

    val visibility = context.resolveVisibility(image.visibilityCode)

    return image.copy(
        url = image.url,
        displayUrl = displayUrl,
        color = imageColor,
        visibility = visibility,
        visibilityCode = image.visibilityCode
    )
}

/** Резолвит толщину (dp) и цвет обводки; общая основа для layout и кнопки. */
private fun StyleResolveContext.resolveStrokeAppearance(
    strokeWidth: String?,
    strokeColorStyleCode: String?,
): Pair<Int, Color>? {
    val rawWidth = strokeWidth ?: return null
    val resolvedWidth = resolveTemplate(rawWidth) ?: rawWidth
    val strokeDp = resolvedWidth.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val rawStrokeStyle = strokeColorStyleCode ?: return null
    val resolvedStrokeStyle = resolveTemplate(rawStrokeStyle) ?: rawStrokeStyle
    val strokeColor = styleRegistry.getComposeColor(resolvedStrokeStyle, useDarkColorPalette) ?: return null
    return strokeDp to strokeColor
}

private fun Modifier.withResolvedBackground(
    backgroundColorStyleCode: String?,
    cornerRadius: CornerRadius,
    context: StyleResolveContext,
): Modifier {
    val color = context.resolveColorStyle(backgroundColorStyleCode) ?: return this
    val shape = cornerRadius.toRoundedCornerShape()
    return if (shape != null) {
        background(color = color, shape = shape)
    } else {
        background(color)
    }
}

private fun Modifier.withResolvedStroke(
    strokeWidth: String?,
    strokeColorStyleCode: String?,
    cornerRadius: CornerRadius,
    context: StyleResolveContext,
): Modifier {
    val stroke = context.resolveStrokeAppearance(
        strokeWidth = strokeWidth,
        strokeColorStyleCode = strokeColorStyleCode,
    ) ?: return this
    val strokeDp = stroke.first
    val strokeColor = stroke.second
    val shape = cornerRadius.toRoundedCornerShape() ?: RoundedCornerShape(0.dp)
    return border(width = strokeDp.dp, color = strokeColor, shape = shape)
}
