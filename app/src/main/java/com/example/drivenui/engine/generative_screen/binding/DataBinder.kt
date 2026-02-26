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
import com.example.drivenui.engine.parser.models.DataContext

private const val TAG = "DataBinder"

object DataBinder {

    /**
     * Применяет биндинги данных к модели экрана
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
        val newVisibilityCode = resolveBindingsInString(label.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: label.visibilityCode)

        return label.copy(
            text = newText ?: label.text,
            textStyleCode = newTextStyleCode ?: label.textStyleCode,
            colorStyleCode = newColorStyleCode ?: label.colorStyleCode,
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
        val newRoundStyleCode = resolveBindingsInString(button.roundStyleCode, dataContext)
        val newVisibilityCode = resolveBindingsInString(button.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: button.visibilityCode)

        return button.copy(
            text = newText ?: button.text,
            textStyleCode = newTextStyleCode ?: button.textStyleCode,
            colorStyleCode = newColorStyleCode ?: button.colorStyleCode,
            backgroundColorStyleCode = newBackgroundColorStyleCode ?: button.backgroundColorStyleCode,
            roundStyleCode = newRoundStyleCode ?: button.roundStyleCode,
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
        val newVisibilityCode = resolveBindingsInString(appBar.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: appBar.visibilityCode)

        return appBar.copy(
            title = newTitle ?: appBar.title,
            textStyleCode = newTextStyleCode ?: appBar.textStyleCode,
            colorStyleCode = newColorStyleCode ?: appBar.colorStyleCode,
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
        // Если это цикл, НЕ применяем биндинги к шаблону (там есть {#forIndexName}, который нужно заменить сначала)
        // Только резолвим maxForIndex
        if (layout.type == LayoutType.VERTICAL_FOR ||
            layout.type == LayoutType.HORIZONTAL_FOR
        ) {
            // НЕ применяем биндинги к шаблону - оставляем как есть
            // Биндинги будут применены в LayoutRenderer после замены {#forIndexName} на индекс

            Log.d(TAG, "Processing FOR loop layout: forIndexName=${layout.forIndexName}, maxForIndex=${layout.maxForIndex}")

            // Резолвим maxForIndex для использования в рендерере
            val maxForIndex = layout.maxForIndex?.let {
                val resolved = resolveMaxForIndex(it, dataContext)
                Log.d(TAG, "Resolved maxForIndex: '$it' -> $resolved")
                resolved
            }

            if (maxForIndex == null) {
                Log.w(TAG, "Failed to resolve maxForIndex for FOR loop: ${layout.maxForIndex}")
            }

            return layout.copy(children = layout.children, maxForIndex = maxForIndex?.toString())
        }

        // Для обычных лейаутов:
        // 1) применяем биндинги к стилям самого лейаута
        // 2) применяем биндинги ко всем детям
        val newBackgroundColorStyleCode =
            resolveBindingsInString(layout.backgroundColorStyleCode, dataContext)
        val newRoundStyleCode =
            resolveBindingsInString(layout.roundStyleCode, dataContext)
        val newVisibilityCode = resolveBindingsInString(layout.visibilityCode, dataContext)
        val visibility = parseVisibility(newVisibilityCode ?: layout.visibilityCode)

        val processedChildren = layout.children.mapNotNull { child ->
            applyBindingsToComponent(child, dataContext)
        }

        return layout.copy(
            children = processedChildren,
            backgroundColorStyleCode = newBackgroundColorStyleCode ?: layout.backgroundColorStyleCode,
            roundStyleCode = newRoundStyleCode ?: layout.roundStyleCode,
            visibility = visibility,
            visibilityCode = newVisibilityCode ?: layout.visibilityCode
        )
    }

    /**
     * Публичный метод для применения биндингов к компоненту (используется в LayoutRenderer для циклов)
     */
    fun applyBindingsToComponentPublic(
        component: ComponentModel,
        dataContext: DataContext
    ): ComponentModel? {
        return applyBindingsToComponent(component, dataContext)
    }

    /**
     * Резолвит maxForIndex из строки (может быть числом или ${...} выражением)
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