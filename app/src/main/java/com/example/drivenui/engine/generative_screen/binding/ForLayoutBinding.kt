package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

private const val TAG = "ForLayoutBinding"

/**
 * Обход дерева компонентов: для `verticalFor` / `horizontalFor` вычисляет
 * `LayoutForParams.resolvedMaxForIndex` из шаблона `LayoutForParams.maxForIndex` и `DataContext`.
 */
object ForLayoutBinding {

    /**
     * Обходит корень экрана и проставляет `resolvedMaxForIndex` для FOR-layout’ов.
     *
     * @param screenModel экран
     * @param dataContext JSON и результаты запросов для `${...}` в `maxForIndex`
     * @return копия экрана с обновлённым деревом
     */
    fun applyBindings(
        screenModel: ScreenModel,
        dataContext: DataContext,
    ): ScreenModel {
        val processedRoot = screenModel.rootComponent?.let { visitComponent(it, dataContext) }
        return screenModel.copy(rootComponent = processedRoot)
    }

    /**
     * То же для одного поддерева (например перед `resolveComponent`).
     *
     * @param component корень поддерева
     * @param dataContext контекст данных
     * @return дерево с `resolvedMaxForIndex` на FOR-layout’ах
     */
    fun applyBindingsToComponent(
        component: ComponentModel,
        dataContext: DataContext,
    ): ComponentModel = visitComponent(component, dataContext)

    private fun visitComponent(
        component: ComponentModel,
        dataContext: DataContext,
    ): ComponentModel =
        when (component) {
            is LayoutModel -> visitLayout(component, dataContext)
            else -> component
        }

    private fun visitLayout(
        layout: LayoutModel,
        dataContext: DataContext,
    ): LayoutModel {
        if (layout.type == LayoutType.VERTICAL_FOR || layout.type == LayoutType.HORIZONTAL_FOR) {
            return applyForLoopResolvedCount(layout, dataContext)
        }

        val processedChildren = layout.children.map { child ->
            visitComponent(child, dataContext)
        }
        return layout.copy(children = processedChildren)
    }

    private fun applyForLoopResolvedCount(
        layout: LayoutModel,
        dataContext: DataContext,
    ): LayoutModel {
        val maxTemplate = layout.forParams.maxForIndex

        val resolvedCount = maxTemplate?.let { template ->
            resolveMaxForIndexToInt(template, dataContext) ?: template.toIntOrNull()
        }
        if (resolvedCount == null) {
            Log.w(TAG, "Не удалось вычислить maxForIndex для FOR: $maxTemplate")
        }

        return layout.copy(
            children = layout.children,
            forParams = layout.forParams.copy(
                resolvedMaxForIndex = resolvedCount?.toString(),
            ),
        )
    }

    private fun resolveMaxForIndexToInt(
        maxForIndexStr: String,
        dataContext: DataContext,
    ): Int? {
        maxForIndexStr.toIntOrNull()?.let {
            return it
        }

        val bindings = DataBindingParser.parseBindings(maxForIndexStr)

        if (bindings.isEmpty()) {
            Log.w(TAG, "В maxForIndex нет биндингов: '$maxForIndexStr'")
            return null
        }

        val resolved = DataBindingParser.replaceBindings(maxForIndexStr, bindings, dataContext)
        val intValue = resolved.toIntOrNull()
        if (intValue == null) {
            Log.w(TAG, "Не удалось привести maxForIndex к int: '$resolved'")
        }
        return intValue
    }
}
