package com.example.drivenui.engine.generative_screen.presentation

import android.util.Log
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.DataBindingParser
import com.example.drivenui.engine.generative_screen.styles.resolveComponent
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.CornerRadius
import com.example.drivenui.engine.uirender.models.LayoutForParams
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.ModifierParams
import com.example.drivenui.engine.uirender.models.RadiusValues
import com.example.drivenui.engine.uirender.utils.expandComponentWithIndex
import androidx.compose.ui.Modifier

/**
 * Разворачивает FOR-layout'ы в обычные контейнеры с полностью resolved-детьми.
 *
 * Каждая итерация цикла становится отдельным layout-обёрткой (как Column/Row в [LayoutRenderer]).
 */
internal object ForPresentationExpander {

    private const val TAG = "ForPresentationExpander"

    /**
     * @property contextManager переменные микроаппа и движка
     * @property styleRegistry реестр стилей
     * @property dataContext JSON и результаты запросов
     * @property useDarkColorPalette ветка цветовой палитры
     */
    data class ExpandContext(
        val contextManager: IContextManager,
        val styleRegistry: ComposeStyleRegistry,
        val dataContext: DataContext,
        val useDarkColorPalette: Boolean,
    )

    /**
     * Разворачивает FOR-layout'ы в обычные контейнеры с resolved-детьми.
     *
     * @param component корень дерева (обычно из definition после FOR-биндингов)
     * @param context контекст резолва
     * @return дерево без VERTICAL_FOR / HORIZONTAL_FOR или null, если [component] был null
     */
    fun expand(component: ComponentModel?, context: ExpandContext): ComponentModel? {
        if (component == null) return null
        return expandNode(component, context)
    }

    private fun expandNode(component: ComponentModel, context: ExpandContext): ComponentModel =
        when (component) {
            is LayoutModel -> expandLayout(component, context)
            else -> component
        }

    private fun expandLayout(layout: LayoutModel, context: ExpandContext): LayoutModel {
        if (!layout.type.isForLayout()) {
            val expandedChildren = layout.children.map { expandNode(it, context) }
            return layout.copy(children = expandedChildren)
        }

        val maxForIndex = resolveMaxForIndex(layout, context)
            ?: return layout.copy(children = emptyList())

        val forIndexName = layout.forParams.forIndexName ?: return layout.copy(children = emptyList())
        val iterationType = layout.type.toIterationLayoutType()
        val containerType = layout.type.toContainerLayoutType()

        val iterationWrappers = buildList {
            repeat(maxForIndex) { index ->
                val indexStr = index.toString()
                val resolvedIterationChildren = layout.children.map { templateChild ->
                    val expanded = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                    val expandedTree = expandNode(expanded, context)
                    resolveComponent(
                        component = expandedTree,
                        contextManager = context.contextManager,
                        styleRegistry = context.styleRegistry,
                        dataContext = context.dataContext,
                        useDarkColorPalette = context.useDarkColorPalette,
                    ) ?: expandedTree
                }
                val iterationWrapper = layout.toIterationWrapper(
                    type = iterationType,
                    children = resolvedIterationChildren,
                )
                val wrapperWithIndex = expandComponentWithIndex(iterationWrapper, forIndexName, indexStr) as LayoutModel
                val resolvedWrapper = resolveComponent(
                    component = wrapperWithIndex,
                    contextManager = context.contextManager,
                    styleRegistry = context.styleRegistry,
                    dataContext = context.dataContext,
                    useDarkColorPalette = context.useDarkColorPalette,
                ) as? LayoutModel ?: wrapperWithIndex
                add(resolvedWrapper)
            }
        }

        return layout.copy(
            type = containerType,
            children = iterationWrappers,
            forParams = LayoutForParams(),
        )
    }

    private fun LayoutType.isForLayout(): Boolean =
        this == LayoutType.VERTICAL_FOR || this == LayoutType.HORIZONTAL_FOR

    private fun LayoutType.toIterationLayoutType(): LayoutType =
        when (this) {
            LayoutType.HORIZONTAL_FOR -> LayoutType.HORIZONTAL_LAYOUT
            else -> LayoutType.VERTICAL_LAYOUT
        }

    private fun LayoutType.toContainerLayoutType(): LayoutType =
        when (this) {
            LayoutType.HORIZONTAL_FOR -> LayoutType.HORIZONTAL_LAYOUT
            else -> LayoutType.VERTICAL_LAYOUT
        }

    /**
     * Обёртка одной итерации — лёгкий Row/Column внутри lazy-контейнера FOR.
     *
     * Не наследует padding, размеры и chrome с FOR-контейнера: в старом [LazyRowRenderer]
     * они применялись один раз на внешний список, а item содержал только детей шаблона.
     */
    /**
     * Число итераций FOR: литерал, [LayoutForParams.resolvedMaxForIndex] или `${...}` из [DataContext].
     *
     * Для вложенного FOR (`${q.groups.[{#i}].doctors.count}`) индекс родителя уже подставлен
     * через [expandComponentWithIndex] к моменту вызова.
     */
    private fun resolveMaxForIndex(layout: LayoutModel, context: ExpandContext): Int? {
        val raw = layout.forParams.resolvedMaxForIndex
            ?: layout.forParams.maxForIndex
            ?: return null

        raw.toIntOrNull()?.let { return it }

        val bindings = DataBindingParser.parseBindings(raw)
        if (bindings.isEmpty()) {
            Log.w(TAG, "Не удалось разобрать maxForIndex: '$raw'")
            return null
        }

        val resolved = DataBindingParser.replaceBindings(raw, bindings, context.dataContext)
        return resolved.toIntOrNull().also { count ->
            if (count == null) {
                Log.w(TAG, "Не удалось привести maxForIndex к int: '$resolved' (из '$raw')")
            }
        }
    }

    private fun LayoutModel.toIterationWrapper(
        type: LayoutType,
        children: List<ComponentModel>,
    ): LayoutModel = copy(
        type = type,
        children = children,
        forParams = LayoutForParams(),
        modifier = Modifier,
        modifierParams = ModifierParams(
            width = "wrapContent",
            height = "wrapContent",
        ),
        backgroundColorStyleCode = null,
        strokeWidth = null,
        strokeColorStyleCode = null,
        radiusValues = RadiusValues(),
        cornerRadius = CornerRadius(),
        onTapActions = emptyList(),
    )
}
