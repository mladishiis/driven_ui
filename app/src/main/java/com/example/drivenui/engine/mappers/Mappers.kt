package com.example.drivenui.engine.mappers

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.getLayoutTypeFromString
import com.example.drivenui.parser.models.Component
import com.example.drivenui.parser.models.LayoutComponent
import com.example.drivenui.parser.models.ParsedScreen
import com.example.drivenui.parser.models.WidgetComponent
import com.example.drivenui.parser.models.WidgetStyle

fun mapParsedScreenToUI(
    screen: ParsedScreen,
    styleRegistry: ComposeStyleRegistry
): ComponentModel? {
    return screen.rootComponent?.mapComponentToUIModel(styleRegistry)
}

fun List<Component>.mapToUiModelList(styleRegistry: ComposeStyleRegistry): List<ComponentModel> =
    mapNotNull {
        it.mapComponentToUIModel(styleRegistry)
    }

fun Component.mapComponentToUIModel(styleRegistry: ComposeStyleRegistry): ComponentModel? {
    val heightProperty = properties.find { it.code == "height" }?.resolvedValue.orEmpty()
    val widthProperty = properties.find { it.code == "width" }?.resolvedValue.orEmpty()
    val paddingStyle = styles.find { it.code == "paddingStyle"}?.value.orEmpty()
    val modifier = Modifier
        .applyPaddingStyle(getPaddingFromCode(paddingStyle, styleRegistry))
        .applyHeight(heightProperty)
        .applyWidth(widthProperty)
    return when (this) {
        is LayoutComponent -> mapLayoutToUIModel(modifier, styleRegistry)
        is WidgetComponent -> mapWidgetToUiModel(modifier, styleRegistry)
    }
}


fun LayoutComponent.mapLayoutToUIModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry) =
    LayoutModel(
        modifier = buildModifierForLayoutFromStyles(modifier, styles, styleRegistry),
        type = getLayoutTypeFromString(layoutCode),
        children = children.mapToUiModelList(styleRegistry),
        onCreateActions = getOnCreateEvents(events),
        onTapActions = getOnTapEvents(events),
        alignmentStyle = getAlignmentStyle()
    )

fun WidgetComponent.mapWidgetToUiModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry): ComponentModel? {
    return when (widgetCode) {
        "appbar" -> {
            mapWidgetToAppbarModel(modifier, styleRegistry)
        }

        "label" -> {
            mapWidgetToLabelModel(modifier, styleRegistry)
        }

        "button" -> {
            mapWidgetToButtonModel(modifier, styleRegistry)
        }

        "image" -> {
            mapWidgetToImageModel(modifier)
        }

        "input" -> {
            mapWidgetToInputModel(modifier, styleRegistry)
        }
// TODO: остальные виджеты

//        "checkbox" -> {
//            CheckboxModel()
//        }
//
//        "switcher" -> {
//            SwitcherModel()
//        }

        else -> {
            //TODO тут какой-то кастомный виджет
            LabelModel(
                modifier = Modifier,
                textStyle = TextStyle.Default,
                text = "Custom Widget",
                widgetCode = widgetCode,
                tapActions = getOnTapEvents(events),
                alignmentStyle = this.getAlignmentStyle()
            )
        }
    }
}

fun WidgetComponent.mapWidgetToLabelModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry): LabelModel? {
    val textProperty = properties.find { it.code == "text" }
    val textColor = getColorFromStyles(styleRegistry)
    return if (textProperty != null) {
        LabelModel(
            modifier = modifier,
            text = textProperty.resolvedValue, // Используем resolvedValue
            textStyle = getTextStyle(textColor, styles, styleRegistry),
            widgetCode = code,
            tapActions = getOnTapEvents(events),
            alignmentStyle = getAlignmentStyle()
        )
    } else null
}


fun getTextStyle(
    color: Color,
    styles: List<WidgetStyle>,
    styleRegistry: ComposeStyleRegistry
): TextStyle {
    val textStyle = TextStyle.Default
    val widgetTextStyle = styles.find { it.code == "textStyle" }
    if (widgetTextStyle == null) return textStyle
    val textStyleFromRegistry = styleRegistry.getTextStyle(widgetTextStyle.value)
    if (textStyleFromRegistry == null) return textStyle
    return textStyle.copy(
        fontSize = textStyleFromRegistry.fontSize.sp,
        fontWeight = FontWeight(textStyleFromRegistry.fontWeight),
        color = color,
    )
}

fun WidgetComponent.mapWidgetToImageModel(modifier: Modifier): ImageModel {
    // TODO может переделать проперти на мапу, чтобы легче доставать?
    val urlProperty = properties.find { it.code == "url" }?.resolvedValue
    return ImageModel(
        modifier = modifier,
        url = urlProperty,
        widgetCode = code,
        tapActions = getOnTapEvents(events),
        alignmentStyle = getAlignmentStyle()
    )
}

fun WidgetComponent.mapWidgetToButtonModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry): ButtonModel {
    val textProperty = properties.find { it.code == "text" }?.resolvedValue ?: ""
    val enabledProperty = properties.find { it.code == "enabled" }?.resolvedValue?.toBoolean() ?: true
    val textColor = getColorFromStyles(styleRegistry)
    return ButtonModel(
        modifier = modifier,
        text = textProperty,
        enabled = enabledProperty,
        textStyle = getTextStyle(textColor, styles, styleRegistry),
        roundedCornerSize = getRoundStyle(styleRegistry),
        background = getBackgroundColorFromStyles(styleRegistry),
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignmentStyle = getAlignmentStyle(),
    )
}

fun WidgetComponent.mapWidgetToAppbarModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry): AppBarModel {
    val titleProperty = properties.find { it.code == "title" }?.resolvedValue ?: ""
    val iconProperty = properties.find { it.code == "leftIconUrl" }?.resolvedValue
    val textColor = getColorFromStyles(styleRegistry)
    return AppBarModel(
        modifier = modifier,
        title = titleProperty,
        textStyle = getTextStyle(textColor, styles, styleRegistry),
        iconLeftUrl = iconProperty,
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignmentStyle = getAlignmentStyle()
    )
}

fun WidgetComponent.mapWidgetToInputModel(modifier: Modifier, styleRegistry: ComposeStyleRegistry): InputModel? {
    val textProperty = properties.find { it.code == "text" }?.resolvedValue ?: ""
    val hintProperty = properties.find { it.code == "hint" }?.resolvedValue ?: ""
    val readOnlyProperty = properties.find { it.code == "readOnly" }?.resolvedValue?.toBoolean() ?: false
    return InputModel(
        modifier = modifier,
        text = textProperty,
        hint = hintProperty,
        readOnly = readOnlyProperty,
        widgetCode = code,
        finishTypingActions = getOnFinishTypingEvents(events),
        alignmentStyle = getAlignmentStyle()
    )
}

private fun WidgetComponent.getColorFromStyles(styleRegistry: ComposeStyleRegistry): Color {
    val colorStyle = styles.find { it.code == "colorStyle" }
    if (colorStyle == null) return Color.Black
    return getColorFromCode(colorStyle.value, styleRegistry)
}

private fun WidgetComponent.getBackgroundColorFromStyles(styleRegistry: ComposeStyleRegistry): Color {
    val colorStyle = styles.find { it.code == "backgroundColorStyle" }
    if (colorStyle == null) return Color.Black
    return getColorFromCode(colorStyle.value, styleRegistry)
}

// TODO проверка темы
private fun getColorFromCode(colorCode: String, styleRegistry: ComposeStyleRegistry): Color {
    return styleRegistry.getColorStyle(colorCode)?.let { colorStyle ->
        Color(colorStyle.lightTheme.color.toColorInt())
    } ?: Color.Black
}

private fun buildModifierForLayoutFromStyles(
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: ComposeStyleRegistry
): Modifier {
    var currentModifier: Modifier = modifier

    styles.forEach { style ->
        currentModifier = when (style.code) {
            "colorStyle" -> {
                val color = getColorFromCode(style.value, styleRegistry)
                currentModifier.background(color)
            }

            else -> currentModifier
        }
    }

    return currentModifier
}