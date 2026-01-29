package com.example.drivenui.engine.mappers

import androidx.compose.ui.Modifier
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
        is WidgetComponent -> mapWidgetToUiModel(modifier)
    }
}


fun LayoutComponent.mapLayoutToUIModel(
    modifier: Modifier,
    styleRegistry: ComposeStyleRegistry
) =
    LayoutModel(
        modifier = modifier,
        type = getLayoutTypeFromString(layoutCode),
        children = children.mapToUiModelList(styleRegistry),
        onCreateActions = getOnCreateEvents(events),
        onTapActions = getOnTapEvents(events),
        backgroundColorStyleCode = styles.find { it.code == "colorStyle" }?.value,
        alignmentStyle = getAlignmentStyle()
    )

fun WidgetComponent.mapWidgetToUiModel(
    modifier: Modifier,
): ComponentModel? {
    return when (widgetCode) {
        "appbar" -> {
            mapWidgetToAppbarModel(modifier)
        }

        "label" -> {
            mapWidgetToLabelModel(modifier)
        }

        "button" -> {
            mapWidgetToButtonModel(modifier)
        }

        "image" -> {
            mapWidgetToImageModel(modifier)
        }

        "input" -> {
            mapWidgetToInputModel(modifier)
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
                text = "Custom Widget",
                widgetCode = widgetCode,
                tapActions = getOnTapEvents(events),
                alignmentStyle = this.getAlignmentStyle()
            )
        }
    }
}

fun WidgetComponent.mapWidgetToLabelModel(
    modifier: Modifier
): LabelModel? {
    val textProperty = properties.find { it.code == "text" }
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    return if (textProperty != null) {
        LabelModel(
            modifier = modifier,
            text = textProperty.resolvedValue, // Используем resolvedValue
            widgetCode = code,
            textStyleCode = textStyleCode,
            colorStyleCode = colorStyleCode,
            tapActions = getOnTapEvents(events),
            alignmentStyle = getAlignmentStyle()
        )
    } else null
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

fun WidgetComponent.mapWidgetToButtonModel(
    modifier: Modifier
): ButtonModel {
    val textProperty = properties.find { it.code == "text" }?.resolvedValue ?: ""
    val enabledProperty = properties.find { it.code == "enabled" }?.resolvedValue?.toBoolean() ?: true
    val roundStyleCode = styles.find { it.code == "roundStyle" }?.value
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val backgroundColorStyleCode = styles.find { it.code == "backgroundColorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    return ButtonModel(
        modifier = modifier,
        text = textProperty,
        enabled = enabledProperty,
        roundedCornerSize = null,
        roundStyleCode = roundStyleCode,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignmentStyle = getAlignmentStyle(),
    )
}

fun WidgetComponent.mapWidgetToAppbarModel(
    modifier: Modifier,
): AppBarModel {
    val titleProperty = properties.find { it.code == "title" }?.resolvedValue ?: ""
    val iconProperty = properties.find { it.code == "leftIconUrl" }?.resolvedValue
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    return AppBarModel(
        modifier = modifier,
        title = titleProperty,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        iconLeftUrl = iconProperty,
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignmentStyle = getAlignmentStyle()
    )
}

fun WidgetComponent.mapWidgetToInputModel(
    modifier: Modifier
): InputModel? {
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