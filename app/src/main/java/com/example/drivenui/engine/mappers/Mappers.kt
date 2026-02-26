package com.example.drivenui.engine.mappers

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.ModifierParams
import com.example.drivenui.engine.uirender.models.getLayoutTypeFromString
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.LayoutComponent
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.parser.models.WidgetComponent

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
    val modifierParams = getModifierParamsFromComponent(this)
    val modifier = modifierParams.toModifier()
    return when (this) {
        is LayoutComponent -> mapLayoutToUIModel(modifier, modifierParams, styleRegistry)
        is WidgetComponent -> mapWidgetToUiModel(modifier, modifierParams),
    }
}


fun LayoutComponent.mapLayoutToUIModel(
    modifier: Modifier,
    modifierParams: ModifierParams,
    styleRegistry: ComposeStyleRegistry
): LayoutModel {
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return LayoutModel(
        modifier = modifier,
        modifierParams = modifierParams,
        type = getLayoutTypeFromString(layoutCode),
        children = children.mapToUiModelList(styleRegistry),
        onCreateActions = getOnCreateEvents(events),
        onTapActions = getOnTapEvents(events),
        backgroundColorStyleCode = styles.find { it.code == "colorStyle" }?.value,
        roundStyleCode = styles.find { it.code == "roundStyle" }?.value,
        alignmentStyle = getAlignmentStyle(),
        forIndexName = forIndexName,
        maxForIndex = maxForIndex,
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

fun WidgetComponent.mapWidgetToUiModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): ComponentModel? {
    return when (widgetCode) {
        "appbar" -> {
            mapWidgetToAppbarModel(modifier, modifierParams)
        },

        "label" -> {
            mapWidgetToLabelModel(modifier, modifierParams)
        },

        "button" -> {
            mapWidgetToButtonModel(modifier, modifierParams)
        },

        "image" -> {
            mapWidgetToImageModel(modifier, modifierParams)
        },

        "input" -> {
            mapWidgetToInputModel(modifier, modifierParams)
        },
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
                modifierParams = modifierParams,
                text = "Custom Widget",
                widgetCode = widgetCode,
                tapActions = getOnTapEvents(events),
                alignmentStyle = this.getAlignmentStyle(),
            )
        },
    }
}

fun WidgetComponent.mapWidgetToLabelModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): LabelModel? {
    val textProperty = properties.find { it.code == "text" }
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return if (textProperty != null) {
        LabelModel(
            modifier = modifier,
            modifierParams = modifierParams,
            text = textProperty.resolvedValue, // Используем resolvedValue
            widgetCode = code,
            textStyleCode = textStyleCode,
            colorStyleCode = colorStyleCode,
            tapActions = getOnTapEvents(events),
            alignmentStyle = getAlignmentStyle(),
            visibility = visibility,
            visibilityCode = visibilityRaw,
        )
    } else null
}


fun WidgetComponent.mapWidgetToImageModel(modifier: Modifier, modifierParams: ModifierParams): ImageModel {
    val urlProperty = properties.find { it.code == "url" }?.resolvedValue
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return ImageModel(
        modifier = modifier,
        modifierParams = modifierParams,
        url = urlProperty,
        widgetCode = code,
        tapActions = getOnTapEvents(events),
        colorStyleCode = colorStyleCode,
        alignmentStyle = getAlignmentStyle(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

fun WidgetComponent.mapWidgetToButtonModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): ButtonModel {
    val textProperty = properties.find { it.code == "text" }?.resolvedValue ?: ""
    val enabledProperty = properties.find { it.code == "enabled" }?.resolvedValue?.toBoolean() ?: true
    val roundStyleCode = styles.find { it.code == "roundStyle" }?.value
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val backgroundColorStyleCode = styles.find { it.code == "backgroundColorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return ButtonModel(
        modifier = modifier,
        modifierParams = modifierParams,
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
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

fun WidgetComponent.mapWidgetToAppbarModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): AppBarModel {
    val titleProperty = properties.find { it.code == "title" }?.resolvedValue ?: ""
    val iconProperty = properties.find { it.code == "leftIconUrl" }?.resolvedValue
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return AppBarModel(
        modifier = modifier,
        modifierParams = modifierParams,
        title = titleProperty,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        iconLeftUrl = iconProperty,
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignmentStyle = getAlignmentStyle(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

fun WidgetComponent.mapWidgetToInputModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): InputModel? {
    val textProperty = properties.find { it.code == "text" }?.resolvedValue ?: ""
    val hintProperty = properties.find { it.code == "hint" }?.resolvedValue ?: ""
    val readOnlyProperty = properties.find { it.code == "readOnly" }?.resolvedValue?.toBoolean() ?: false
    val visibilityRaw = properties.find { it.code == "visibility" }?.resolvedValue
    val visibility = parseVisibility(visibilityRaw)
    return InputModel(
        modifier = modifier,
        modifierParams = modifierParams,
        text = textProperty,
        hint = hintProperty,
        readOnly = readOnlyProperty,
        widgetCode = code,
        finishTypingActions = getOnFinishTypingEvents(events),
        alignmentStyle = getAlignmentStyle(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}