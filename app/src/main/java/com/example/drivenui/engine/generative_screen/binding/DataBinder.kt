package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.mappers.parseVisibility
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.RadiusValues
import com.example.drivenui.engine.parser.models.DataContext

private const val TAG = "DataBinder"

/**
 * Применяет биндинги данных к моделям экранов и компонентов.
 *
 * Подставляет значения из [DataContext] в строковые поля (text, visibilityCode, коды стилей и т.д.)
 * по шаблонам ${...}. Используется до резолва стилей и рендеринга.
 */
object DataBinder {

    /**
     * Применяет биндинги данных к модели экрана.
     *
     * @param screenModel Модель экрана
     * @param dataContext Контекст данных для биндингов
     * @return Обновлённая модель экрана
     */
    fun applyBindings(
        screenModel: ScreenModel,
        dataContext: DataContext
    ): ScreenModel {
        Log.d(TAG, "Applying bindings for screen: ${screenModel.id}")
        Log.d(TAG, "DataContext jsonSources: ${dataContext.jsonSources.keys}")

        val processedRootComponent = screenModel.rootComponent?.let {
            applyBindingsToComponent(it, dataContext)
        }

        return screenModel.copy(
            rootComponent = processedRootComponent
        )
    }

    /**
     * Рекурсивно применяет биндинги к компоненту и его детям
     */
    private fun applyBindingsToComponent(
        component: ComponentModel?,
        dataContext: DataContext
    ): ComponentModel? {
        return when (component) {
            is LabelModel -> applyBindingsToLabel(component, dataContext)
            is ButtonModel -> applyBindingsToButton(component, dataContext)
            is AppBarModel -> applyBindingsToAppBar(component, dataContext)
            is ImageModel -> applyBindingsToImage(component, dataContext)
            is InputModel -> applyBindingsToInput(component, dataContext)
            is LayoutModel -> applyBindingsToLayout(component, dataContext)
            else -> component
        }
    }

    private fun applyBindingsToLabel(
        label: LabelModel,
        dataContext: DataContext
    ): LabelModel {
        val newText = resolveBindingsInString(label.text, dataContext)
        val newTextStyleCode = resolveBindingsInString(label.textStyleCode, dataContext)
        val newColorStyleCode = resolveBindingsInString(label.colorStyleCode, dataContext)
        val newTextAlignmentStyle = resolveBindingsInString(label.textAlignment, dataContext)
        val newVisibilityCode = resolveBindingsInString(label.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: label.visibilityCode)

        return label.copy(
            text = newText ?: label.text,
            textStyleCode = newTextStyleCode ?: label.textStyleCode,
            colorStyleCode = newColorStyleCode ?: label.colorStyleCode,
            textAlignment = newTextAlignmentStyle ?: label.textAlignment,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: label.visibilityCode
        )
    }

    private fun applyBindingsToButton(
        button: ButtonModel,
        dataContext: DataContext
    ): ButtonModel {
        val newText = resolveBindingsInString(button.text, dataContext)
        val newTextStyleCode = resolveBindingsInString(button.textStyleCode, dataContext)
        val newColorStyleCode = resolveBindingsInString(button.colorStyleCode, dataContext)
        val newBackgroundColorStyleCode = resolveBindingsInString(button.backgroundColorStyleCode, dataContext)
        val newRadiusValues = RadiusValues(
            radius = resolveBindingsInString(button.radiusValues.radius, dataContext) ?: button.radiusValues.radius,
            radiusTop = resolveBindingsInString(button.radiusValues.radiusTop, dataContext) ?: button.radiusValues.radiusTop,
            radiusBottom = resolveBindingsInString(button.radiusValues.radiusBottom, dataContext) ?: button.radiusValues.radiusBottom,
        )
        val newTextAlignmentStyle = resolveBindingsInString(button.textAlignment, dataContext)
        val newVisibilityCode = resolveBindingsInString(button.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: button.visibilityCode)

        return button.copy(
            text = newText ?: button.text,
            textStyleCode = newTextStyleCode ?: button.textStyleCode,
            colorStyleCode = newColorStyleCode ?: button.colorStyleCode,
            backgroundColorStyleCode = newBackgroundColorStyleCode ?: button.backgroundColorStyleCode,
            radiusValues = newRadiusValues,
            textAlignment = newTextAlignmentStyle ?: button.textAlignment,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: button.visibilityCode
        )
    }

    private fun applyBindingsToAppBar(
        appBar: AppBarModel,
        dataContext: DataContext
    ): AppBarModel {
        val newTitle = appBar.title?.let { resolveBindingsInString(it, dataContext) }
        val newTextStyleCode = resolveBindingsInString(appBar.textStyleCode, dataContext)
        val newColorStyleCode = resolveBindingsInString(appBar.colorStyleCode, dataContext)
        val newLeftIconColorStyleCode = resolveBindingsInString(appBar.leftIconColorStyleCode, dataContext)
        val newBackgroundColorStyleCode = resolveBindingsInString(appBar.backgroundColorStyleCode, dataContext)
        val newVisibilityCode = resolveBindingsInString(appBar.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: appBar.visibilityCode)

        return appBar.copy(
            title = newTitle ?: appBar.title,
            textStyleCode = newTextStyleCode ?: appBar.textStyleCode,
            colorStyleCode = newColorStyleCode ?: appBar.colorStyleCode,
            leftIconColorStyleCode = newLeftIconColorStyleCode ?: appBar.leftIconColorStyleCode,
            backgroundColorStyleCode = newBackgroundColorStyleCode ?: appBar.backgroundColorStyleCode,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: appBar.visibilityCode
        )
    }

    private fun applyBindingsToInput(
        input: InputModel,
        dataContext: DataContext
    ): InputModel {
        val newText = resolveBindingsInString(input.text, dataContext)
        val newHint = resolveBindingsInString(input.hint, dataContext)
        val newWidgetCode = resolveBindingsInString(input.widgetCode, dataContext)
        val newVisibilityCode = resolveBindingsInString(input.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: input.visibilityCode)

        return input.copy(
            text = newText ?: input.text,
            hint = newHint ?: input.hint,
            widgetCode = newWidgetCode ?: input.widgetCode,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: input.visibilityCode
        )
    }

    private fun applyBindingsToImage(
        image: ImageModel,
        dataContext: DataContext
    ): ImageModel {
        val newUrl = resolveBindingsInString(image.url, dataContext)
        val newColorStyleCode = resolveBindingsInString(image.colorStyleCode, dataContext)
        val newVisibilityCode = resolveBindingsInString(image.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: image.visibilityCode)

        return image.copy(
            url = newUrl ?: image.url,
            colorStyleCode = newColorStyleCode ?: image.colorStyleCode,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: image.visibilityCode
        )
    }

    private fun applyBindingsToLayout(
        layout: LayoutModel,
        dataContext: DataContext
    ): LayoutModel {
        if (layout.type == LayoutType.VERTICAL_FOR ||
            layout.type == LayoutType.HORIZONTAL_FOR
        ) {
            Log.d(TAG, "Processing FOR loop layout: forIndexName=${layout.forParams.forIndexName}, maxForIndex=${layout.forParams.maxForIndex}")
            val maxForIndex = layout.forParams.maxForIndex?.let {
                val resolved = resolveMaxForIndex(it, dataContext)
                Log.d(TAG, "Resolved maxForIndex: '$it' -> $resolved")
                resolved
            }

            if (maxForIndex == null) {
                Log.w(TAG, "Failed to resolve maxForIndex for FOR loop: ${layout.forParams.maxForIndex}")
            }

            return layout.copy(
                children = layout.children,
                forParams = layout.forParams.copy(maxForIndex = maxForIndex?.toString()),
            )
        }

        val newBackgroundColorStyleCode =
            resolveBindingsInString(layout.backgroundColorStyleCode, dataContext)
        val newRadiusValues = layout.radiusValues.let { rv: RadiusValues ->
            RadiusValues(
                radius = resolveBindingsInString(rv.radius, dataContext) ?: rv.radius,
                radiusTop = resolveBindingsInString(rv.radiusTop, dataContext) ?: rv.radiusTop,
                radiusBottom = resolveBindingsInString(rv.radiusBottom, dataContext) ?: rv.radiusBottom,
            )
        }
        val newVisibilityCode = resolveBindingsInString(layout.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: layout.visibilityCode)

        val processedChildren = layout.children.mapNotNull { child ->
            applyBindingsToComponent(child, dataContext)
        }

        return layout.copy(
            children = processedChildren,
            backgroundColorStyleCode = newBackgroundColorStyleCode ?: layout.backgroundColorStyleCode,
            radiusValues = newRadiusValues,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: layout.visibilityCode,
        )
    }

    /**
     * Публичный метод для применения биндингов к компоненту (используется в LayoutRenderer для циклов).
     *
     * @param component Компонент для применения биндингов
     * @param dataContext Контекст данных
     * @return Обновлённый компонент или null
     */
    fun applyBindingsToComponentPublic(
        component: ComponentModel,
        dataContext: DataContext
    ): ComponentModel? {
        return applyBindingsToComponent(component, dataContext)
    }

    /**
     * Резолвит maxForIndex из строки (может быть числом или ${...} выражением).
     *
     * @param maxForIndexStr Исходная строка (число или выражение)
     * @param dataContext Контекст данных
     * @return Int или null при ошибке
     */
    private fun resolveMaxForIndex(maxForIndexStr: String, dataContext: DataContext): Int? {
        Log.d(TAG, "Resolving maxForIndex: '$maxForIndexStr'")

        maxForIndexStr.toIntOrNull()?.let {
            Log.d(TAG, "maxForIndex is already a number: $it")
            return it
        }

        val bindings = DataBindingParser.parseBindings(maxForIndexStr)
        Log.d(TAG, "Parsed bindings for maxForIndex: $bindings")
        Log.d(TAG, "DataContext screenQueryResults keys: ${dataContext.screenQueryResults.keys}")
        Log.d(TAG, "DataContext jsonSources keys: ${dataContext.jsonSources.keys}")

        if (bindings.isNotEmpty()) {
            val resolved = DataBindingParser.replaceBindings(maxForIndexStr, bindings, dataContext)
            Log.d(TAG, "Resolved maxForIndex expression: '$maxForIndexStr' -> '$resolved'")
            val intValue = resolved.toIntOrNull()
            if (intValue == null) {
                Log.w(TAG, "Failed to convert resolved maxForIndex to int: '$resolved'")
            }
            return intValue
        }

        Log.w(TAG, "No bindings found in maxForIndex: '$maxForIndexStr'")
        return null
    }

    /**
     * Если в строке есть биндинги ${}, подставляет их через DataBindingParser.
     * Возвращает новую строку или null, если биндингов нет.
     *
     * @param value Исходная строка
     * @param dataContext Контекст данных
     * @return Новая строка с подставленными биндингами или null
     */
    private fun resolveBindingsInString(
        value: String?,
        dataContext: DataContext
    ): String? {
        if (value.isNullOrEmpty()) return null
        val bindings = DataBindingParser.parseBindings(value)
        if (bindings.isEmpty()) return null
        return DataBindingParser.replaceBindings(value, bindings, dataContext)
    }
}