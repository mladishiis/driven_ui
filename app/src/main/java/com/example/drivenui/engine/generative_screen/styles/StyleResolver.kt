package com.example.drivenui.engine.generative_screen.styles

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
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
    val resolvedChildren = layout.children.mapNotNull {
        resolveComponent(it, contextManager, styleRegistry)
    }

    var modifier = layout.modifier

    layout.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val colorStyle = styleRegistry.getColorStyle(resolvedCode)
        if (colorStyle != null) {
            val color = Color(colorStyle.lightTheme.color.toColorInt())
            modifier = modifier.background(color)
        }
    }

    return layout.copy(
        modifier = modifier,
        children = resolvedChildren
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
        // На случай, если roundStyleCode тоже станет условным или контекстным
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
        val cs = styleRegistry.getColorStyle(resolvedCode)
        if (cs != null) {
            val color = Color(cs.lightTheme.color.toColorInt())
            textStyle = textStyle.copy(color = color)
        }
    }

    button.backgroundColorStyleCode?.let { rawCode ->
        val resolvedCode = resolveValueExpression(rawCode, contextManager)
        val cs = styleRegistry.getColorStyle(resolvedCode)
        if (cs != null) {
            backgroundColor = Color(cs.lightTheme.color.toColorInt())
        }
    }

    return button.copy(
        text = resolvedText,
        roundedCornerSize = roundedCornerSize,
        textStyle = textStyle,
        backgroundColor = backgroundColor
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
        val cs = styleRegistry.getColorStyle(resolvedCode)
        if (cs != null) {
            val color = Color(cs.lightTheme.color.toColorInt())
            textStyle = textStyle.copy(color = color)
        }
    }

    return label.copy(
        text = resolvedText,
        textStyle = textStyle
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
        val cs = styleRegistry.getColorStyle(resolvedCode)
        if (cs != null) {
            val color = Color(cs.lightTheme.color.toColorInt())
            textStyle = textStyle.copy(color = color)
        }
    }

    return appBar.copy(
        title = resolvedTitle,
        textStyle = textStyle
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

    return input.copy(
        text = resolvedText,
        hint = resolvedHint
    )
}

private fun resolveImage(
    image: ImageModel,
    contextManager: IContextManager,
    styleRegistry: ComposeStyleRegistry
): ImageModel {
    // Резолвим текстовые поля
    val resolvedUrl = image.url?.let { resolveValueExpression(it, contextManager) }

    return image.copy(url = resolvedUrl)
}
