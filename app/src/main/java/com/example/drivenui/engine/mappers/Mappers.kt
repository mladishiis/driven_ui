package com.example.drivenui.engine.mappers

import androidx.compose.ui.Modifier
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.LayoutComponent
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.parser.models.WidgetComponent
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ButtonStrokeStyle
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutForParams
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.ModifierParams
import com.example.drivenui.engine.uirender.models.RadiusValues
import com.example.drivenui.engine.uirender.models.getLayoutTypeFromString

/**
 * Преобразует ParsedScreen в ComponentModel для рендеринга.
 *
 * @param screen Распарсенный экран
 * @param styleRegistry Реестр стилей
 * @return Корневой ComponentModel или null
 */
fun mapParsedScreenToUI(
    screen: ParsedScreen,
    styleRegistry: ComposeStyleRegistry
): ComponentModel? {
    return screen.rootComponent?.mapComponentToUIModel(styleRegistry)
}

/**
 * Преобразует список компонентов в список ComponentModel.
 *
 * @receiver Список компонентов
 * @param styleRegistry Реестр стилей
 * @return Список ComponentModel
 */
fun List<Component>.mapToUiModelList(styleRegistry: ComposeStyleRegistry): List<ComponentModel> =
    mapNotNull {
        it.mapComponentToUIModel(styleRegistry)
    }

/**
 * Преобразует компонент в ComponentModel для рендеринга.
 *
 * @receiver Исходный компонент
 * @param styleRegistry Реестр стилей
 * @return ComponentModel или null
 */
fun Component.mapComponentToUIModel(styleRegistry: ComposeStyleRegistry): ComponentModel? {
    val modifierParams = getModifierParamsFromComponent(this)
    return when (this) {
        is LayoutComponent -> mapLayoutToUIModel(Modifier, modifierParams, styleRegistry)
        is WidgetComponent -> mapWidgetToUiModel(Modifier, modifierParams)
    }
}


/**
 * Преобразует LayoutComponent в LayoutModel.
 *
 * @receiver LayoutComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @param styleRegistry Реестр стилей
 * @return LayoutModel
 */
fun LayoutComponent.mapLayoutToUIModel(
    modifier: Modifier,
    modifierParams: ModifierParams,
    styleRegistry: ComposeStyleRegistry
): LayoutModel {
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    val layoutType = getLayoutTypeFromString(layoutCode)
    val scrollExplicit = properties["scroll"]?.trim()?.lowercase()
    val isScrollable = when (scrollExplicit) {
        "true" -> true
        "false" -> false
        else -> layoutType == LayoutType.VERTICAL_LAYOUT
    }
    val finalModifierParams = modifierParams.copy(scrollable = isScrollable)
    return LayoutModel(
        modifier = modifier,
        modifierParams = finalModifierParams,
        type = layoutType,
        children = children.mapToUiModelList(styleRegistry),
        onTapActions = getOnTapEvents(events),
        backgroundColorStyleCode = styles.find { it.code == "backgroundColorStyle" }?.value,
        strokeWidth = properties["strokeWidth"],
        strokeColorStyleCode = styles.find { it.code == "strokeColorStyle" }?.value,
        radiusValues = RadiusValues(
            radius = properties["radius"],
            radiusTop = properties["radiusTop"],
            radiusBottom = properties["radiusBottom"],
        ),
        forParams = LayoutForParams(forIndexName = forIndexName, maxForIndex = maxForIndex),
        alignment = getAlignment(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

/**
 * Преобразует WidgetComponent в ComponentModel.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return ComponentModel или null
 */
fun WidgetComponent.mapWidgetToUiModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): ComponentModel? {
    return when (widgetCode.lowercase()) {
        "appbar" -> {
            mapWidgetToAppbarModel(modifier, modifierParams)
        }

        "label" -> {
            mapWidgetToLabelModel(modifier, modifierParams)
        }

        "button" -> {
            mapWidgetToButtonModel(modifier, modifierParams)
        }

        "image" -> {
            mapWidgetToImageModel(modifier, modifierParams)
        }

        "input" -> {
            mapWidgetToInputModel(modifier, modifierParams)
        }

        else -> {
            LabelModel(
                modifier = Modifier,
                modifierParams = modifierParams,
                text = "Custom Widget",
                widgetCode = widgetCode,
                tapActions = emptyList(),
                alignment = this.getAlignment(),
            )
        }
    }
}

/**
 * Преобразует WidgetComponent в LabelModel.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return LabelModel или null
 */
fun WidgetComponent.mapWidgetToLabelModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): LabelModel? {
    val text = properties["text"]
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    return if (text != null) {
        LabelModel(
            modifier = modifier,
            modifierParams = modifierParams,
            text = text,
            widgetCode = code,
            textStyleCode = textStyleCode,
            colorStyleCode = colorStyleCode,
            tapActions = getOnTapEvents(events),
            alignment = getAlignment(),
            textAlignment = getTextAlignment().ifBlank { "alignLeft" },
            visibility = visibility,
            visibilityCode = visibilityRaw,
        )
    } else null
}


/**
 * Преобразует WidgetComponent в ImageModel.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return ImageModel
 */
fun WidgetComponent.mapWidgetToImageModel(modifier: Modifier, modifierParams: ModifierParams): ImageModel {
    val urlProperty = properties["url"]
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    return ImageModel(
        modifier = modifier,
        modifierParams = modifierParams,
        url = urlProperty,
        widgetCode = code,
        tapActions = getOnTapEvents(events),
        colorStyleCode = colorStyleCode,
        alignment = getAlignment(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

/**
 * Преобразует WidgetComponent в ButtonModel.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return ButtonModel
 */
fun WidgetComponent.mapWidgetToButtonModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): ButtonModel {
    val textProperty = properties["text"] ?: ""
    val enabledProperty = properties["enabled"]?.toBoolean() ?: true
    val radiusValues = RadiusValues(
        radius = properties["radius"],
        radiusTop = properties["radiusTop"],
        radiusBottom = properties["radiusBottom"],
    )
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val backgroundColorStyleCode = styles.find { it.code == "backgroundColorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    return ButtonModel(
        modifier = modifier,
        modifierParams = modifierParams,
        text = textProperty,
        enabled = enabledProperty,
        radiusValues = radiusValues,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        stroke = ButtonStrokeStyle(
            width = properties["strokeWidth"],
            colorStyleCode = styles.find { it.code == "strokeColorStyle" }?.value,
        ),
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignment = getAlignment(),
        textAlignment = getTextAlignment().ifBlank { "alignCenter" },
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

/**
 * Преобразует WidgetComponent в AppBarModel.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return AppBarModel
 */
fun WidgetComponent.mapWidgetToAppbarModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): AppBarModel {
    val titleProperty = properties["title"] ?: ""
    val iconProperty = properties["leftIconUrl"]
    val colorStyleCode = styles.find { it.code == "colorStyle" }?.value
    val textStyleCode = styles.find { it.code == "textStyle" }?.value
    val leftIconColorStyleCode = styles.find { it.code == "leftIconColorStyle" }?.value
    val backgroundColorStyleCode = styles.find { it.code == "backgroundColorStyle" }?.value
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    return AppBarModel(
        modifier = modifier,
        modifierParams = modifierParams,
        title = titleProperty,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        leftIconColorStyleCode = leftIconColorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        iconLeftUrl = iconProperty,
        tapActions = getOnTapEvents(events),
        widgetCode = code,
        alignment = getAlignment(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}

/**
 * Преобразует WidgetComponent в InputModel.
 * Из событий маппятся `onTap` и `onFinishTyping`; `onTyping`, `onFocus`, `onFinishFocus` — позже.
 *
 * @receiver WidgetComponent
 * @param modifier Базовый Modifier
 * @param modifierParams Параметры модификатора
 * @return InputModel или null
 */
fun WidgetComponent.mapWidgetToInputModel(
    modifier: Modifier,
    modifierParams: ModifierParams
): InputModel? {
    val textProperty = properties["text"] ?: ""
    val hintProperty = properties["hint"] ?: ""
    val readOnlyProperty = properties["readOnly"]?.toBoolean() ?: false
    val visibilityRaw = properties["visibility"]
    val visibility = parseVisibility(visibilityRaw)
    return InputModel(
        modifier = modifier,
        modifierParams = modifierParams,
        text = textProperty,
        hint = hintProperty,
        readOnly = readOnlyProperty,
        widgetCode = code,
        finishTypingActions = getOnFinishTypingEvents(events),
        tapActions = getOnTapEvents(events),
        alignment = getAlignment(),
        visibility = visibility,
        visibilityCode = visibilityRaw,
    )
}