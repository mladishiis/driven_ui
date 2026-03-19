package com.example.drivenui.engine.cache

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.LayoutForParams
import com.example.drivenui.engine.uirender.models.RadiusValues
import com.example.drivenui.engine.uirender.models.*

/**
 * Конвертирует [ComponentModel] в [CachedComponentModel] для сохранения в кэш.
 *
 * @receiver компонент для сериализации
 * 
 * @return сериализуемое представление компонента
 */
fun ComponentModel.toCached(): CachedComponentModel = when (this) {
    is LayoutModel -> CachedLayoutModel(
        modifierParams = modifierParams,
        type = type,
        children = children.map { it.toCached() },
        onCreateActions = onCreateActions,
        onTapActions = onTapActions,
        backgroundColorStyleCode = backgroundColorStyleCode,
        radius = radiusValues.radius,
        radiusTop = radiusValues.radiusTop,
        radiusBottom = radiusValues.radiusBottom,
        alignment = alignment,
        forIndexName = forParams.forIndexName,
        maxForIndex = forParams.maxForIndex,
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
        alignment = alignment,
        textAlignment = textAlignment,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is ButtonModel -> CachedButtonModel(
        modifierParams = modifierParams,
        enabled = enabled,
        text = text,
        radius = radiusValues.radius,
        radiusTop = radiusValues.radiusTop,
        radiusBottom = radiusValues.radiusBottom,
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignment = alignment,
        textAlignment = textAlignment,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is ImageModel -> CachedImageModel(
        modifierParams = modifierParams,
        url = url,
        widgetCode = widgetCode,
        tapActions = tapActions,
        colorStyleCode = colorStyleCode,
        alignment = alignment,
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
        alignment = alignment,
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
        alignment = alignment,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    else -> throw IllegalArgumentException("Unsupported ComponentModel: $this")
}

/**
 * Конвертирует [CachedScreenModel] в [ScreenModel].
 *
 * @receiver закэшированная модель экрана
 * 
 * @return модель экрана для рендеринга
 */
fun CachedScreenModel.toScreenModel(): ScreenModel =
    ScreenModel(
        id = id,
        deeplink = deeplink,
        requests = requests,
        rootComponent = rootComponent?.toComponentModel()
    )

/**
 * Конвертирует [ScreenModel] в [CachedScreenModel] для сохранения.
 *
 * @receiver модель экрана для сериализации
 * 
 * @return сериализуемое представление экрана
 */
fun ScreenModel.toCachedScreenModel(): CachedScreenModel =
    CachedScreenModel(
        id = id,
        deeplink = deeplink,
        requests = requests,
        rootComponent = rootComponent?.toCached()
    )

/**
 * Конвертирует [CachedComponentModel] в [ComponentModel] при загрузке из кэша.
 * TextStyle и Color восстанавливаются из кодов через defaults; resolveComponent применит стили при отображении.
 *
 * @receiver закэшированная модель компонента
 *
 * @return Модель компонента для рендеринга
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
        radiusValues = RadiusValues(
            radius = radius,
            radiusTop = radiusTop,
            radiusBottom = radiusBottom,
        ),
        forParams = LayoutForParams(forIndexName = forIndexName, maxForIndex = maxForIndex),
        alignment = alignment,
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
        alignment = alignment,
        textAlignment = textAlignment,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
    is CachedButtonModel -> ButtonModel(
        modifier = Modifier,
        modifierParams = modifierParams,
        enabled = enabled,
        text = text,
        textStyle = TextStyle.Default,
        backgroundColor = Color.Black,
        radiusValues = RadiusValues(
            radius = radius,
            radiusTop = radiusTop,
            radiusBottom = radiusBottom,
        ),
        textStyleCode = textStyleCode,
        colorStyleCode = colorStyleCode,
        backgroundColorStyleCode = backgroundColorStyleCode,
        tapActions = tapActions,
        widgetCode = widgetCode,
        alignment = alignment,
        textAlignment = textAlignment,
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
        alignment = alignment,
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
        alignment = alignment,
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
        alignment = alignment,
        visibility = visibility,
        visibilityCode = visibilityCode
    )
}
