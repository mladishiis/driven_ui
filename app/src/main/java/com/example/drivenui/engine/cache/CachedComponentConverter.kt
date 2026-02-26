package com.example.drivenui.engine.cache

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.*

/**
 * Конвертирует ComponentModel в CachedComponentModel (для сохранения).
 */
fun ComponentModel.toCached(): CachedComponentModel = when (this) {
    is LayoutModel -> CachedLayoutModel(
        modifierParams = modifierParams,
        type = type,
        children = children.map { it.toCached() },
        onCreateActions = onCreateActions,
        onTapActions = onTapActions,
        backgroundColorStyleCode = backgroundColorStyleCode,
        roundStyleCode = roundStyleCode,
        alignmentStyle = alignmentStyle,
        forIndexName = forIndexName,
        maxForIndex = maxForIndex,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is LabelModel -> CachedLabelModel(
        modifierParams = modifierParams,
        text = text,
        widgetCode = widgetCode,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        tapActions = tapActions,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is ButtonModel -> CachedButtonModel(
        modifierParams = modifierParams,
        enabled = enabled,
        text = text,
        roundedCornerSize = roundedCornerSize,
        roundStyleCode = roundStyleCode,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is ImageModel -> CachedImageModel(
        modifierParams = modifierParams,
        url = url,
        widgetCode = widgetCode,
        tapActions = tapActions,
        colorStyleCode = colorStyleCode,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is AppBarModel -> CachedAppBarModel(
        modifierParams = modifierParams,
        title = title,
        iconLeftUrl = iconLeftUrl,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is InputModel -> CachedInputModel(
        modifierParams = modifierParams,
        text = text,
        hint = hint,
        readOnly = readOnly,
        widgetCode = widgetCode,
        finishTypingActions = finishTypingActions,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    else -> throw IllegalArgumentException("Unsupported ComponentModel: $this")
}

/** Конвертирует CachedScreenModel в ScreenModel (generative_screen.models.ScreenModel). */
fun CachedScreenModel.toScreenModel(): ScreenModel =
    ScreenModel(
        id = id,
        requests = requests,
        rootComponent = rootComponent?.toComponentModel()
    )

/** Конвертирует ScreenModel в CachedScreenModel. */
fun ScreenModel.toCachedScreenModel(): CachedScreenModel =
    CachedScreenModel(
        id = id,
        requests = requests,
        rootComponent = rootComponent?.toCached()
    )

/**
 * Конвертирует CachedComponentModel в ComponentModel (для загрузки).
 * TextStyle и Color восстанавливаются из кодов через defaults; resolveComponent применит стили при отображении.
 */
fun CachedComponentModel.toComponentModel(): ComponentModel = when (this) {
    is CachedLayoutModel -> LayoutModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        type = type,
        children = children.map { it.toComponentModel() },
        onCreateActions = onCreateActions,
        onTapActions = onTapActions,
        backgroundColorStyleCode = backgroundColorStyleCode,
        roundStyleCode = roundStyleCode,
        alignmentStyle = alignmentStyle,
        forIndexName = forIndexName,
        maxForIndex = maxForIndex,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedLabelModel -> LabelModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        text = text,
        widgetCode = widgetCode,
        textStyle = TextStyle.Default,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        tapActions = tapActions,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedButtonModel -> ButtonModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        enabled = enabled,
        text = text,
        roundedCornerSize = roundedCornerSize,
        textStyle = TextStyle.Default,
        backgroundColor = Color.Black,
        roundStyleCode = roundStyleCode,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedImageModel -> ImageModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        url = url,
        widgetCode = widgetCode,
        tapActions = tapActions,
        colorStyleCode = colorStyleCode,
        color = Color.Unspecified,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedAppBarModel -> AppBarModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        title = title,
        iconLeftUrl = iconLeftUrl,
        textStyle = TextStyle.Default,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedInputModel -> InputModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        text = text,
        hint = hint,
        readOnly = readOnly,
        widgetCode = widgetCode,
        finishTypingActions = finishTypingActions,
        alignmentStyle = alignmentStyle,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
}
