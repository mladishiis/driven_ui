package com.example.drivenui.engine.generative_screen.refresh

import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.ForLayoutBinding
import com.example.drivenui.engine.generative_screen.models.ScreenDefinition
import com.example.drivenui.engine.generative_screen.models.ScreenPresentation
import com.example.drivenui.engine.generative_screen.presentation.PresentationBuilder
import com.example.drivenui.engine.generative_screen.styles.resolveComponent
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.utils.expandComponentWithIndex

/**
 * Результат точечного обновления экрана.
 *
 * @property definition шаблон с актуальными FOR-биндингами
 * @property presentation resolved-дерево с обновлённым узлом
 */
data class TargetedRefreshResult(
    val definition: ScreenDefinition,
    val presentation: ScreenPresentation,
)

/**
 * Контекст для резолва одного узла дерева.
 *
 * @property contextManager переменные микроаппа и движка
 * @property styleRegistry реестр стилей
 * @property dataContext JSON и результаты запросов
 * @property useDarkColorPalette ветка цветовой палитры (светлая/тёмная)
 */
data class RefreshResolveContext(
    val contextManager: IContextManager,
    val styleRegistry: ComposeStyleRegistry,
    val dataContext: DataContext,
    val useDarkColorPalette: Boolean,
)

/**
 * Критерий поиска узла в дереве компонентов для точечного refresh.
 *
 * Используется только внутри [refreshComponentInScreen]: путь строится в presentation-дереве,
 * источник для резолва — в definition-дереве (с expand FOR при необходимости).
 *
 * Коды берутся из JSON-файла экрана (папка `screens` архива микроаппа): поле `id` или `code` узла.
 *
 * @see refreshWidgetInScreen — обёртка над [Widget]
 * @see refreshLayoutInScreen — обёртка над [Layout]
 */
internal sealed interface ComponentLocator {

    /**
     * Поиск виджета: label, button, image, input, appbar и т.д.
     *
     * @property code [LabelModel.widgetCode] (и аналогичное поле у других виджетов) —
     * совпадает с `id`/`code` узла в JSON экрана
     */
    data class Widget(val code: String) : ComponentLocator

    /**
     * Поиск layout-контейнера: vertical, horizontal, layers, verticalFor и т.д.
     *
     * @property code [LayoutModel.layoutCode] — совпадает с `id`/`code` узла type=LAYOUT в JSON экрана
     */
    data class Layout(val code: String) : ComponentLocator
}

/**
 * Перерезолвит один виджет по [widgetCode] и подставляет его в presentation.
 *
 * @param definition шаблон экрана (фаза Definition)
 * @param presentation текущее resolved-дерево (фаза Presentation)
 * @param widgetCode код виджета из JSON-файла экрана (поле id/code узла)
 * @param resolveContext контекст резолва
 * @return обновлённые definition и presentation или null, если виджет не найден
 */
fun refreshWidgetInScreen(
    definition: ScreenDefinition,
    presentation: ScreenPresentation,
    widgetCode: String,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? =
    refreshComponentInScreen(
        definition = definition,
        presentation = presentation,
        locator = ComponentLocator.Widget(widgetCode),
        resolveContext = resolveContext,
    )

/**
 * Перерезолвит один layout по [layoutCode] (включая дочернее поддерево).
 *
 * @param definition шаблон экрана (фаза Definition)
 * @param presentation текущее resolved-дерево (фаза Presentation)
 * @param layoutCode код layout из JSON-файла экрана (поле id/code узла)
 * @param resolveContext контекст резолва
 * @return обновлённые definition и presentation или null, если layout не найден
 */
fun refreshLayoutInScreen(
    definition: ScreenDefinition,
    presentation: ScreenPresentation,
    layoutCode: String,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? =
    refreshComponentInScreen(
        definition = definition,
        presentation = presentation,
        locator = ComponentLocator.Layout(layoutCode),
        resolveContext = resolveContext,
    )

private fun refreshComponentInScreen(
    definition: ScreenDefinition,
    presentation: ScreenPresentation,
    locator: ComponentLocator,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? {
    val presentationRoot = presentation.rootComponent ?: return null
    val presentationPath = findComponentPath(presentationRoot, locator) ?: return null

    val definitionWithBindings = ForLayoutBinding.applyBindings(
        definition,
        resolveContext.dataContext,
    )
    val definitionRoot = definitionWithBindings.rootComponent ?: return null

    val sourceComponent = resolveSourceComponent(definitionRoot, locator) ?: return null
    val resolvedComponent = resolveComponent(
        component = sourceComponent,
        contextManager = resolveContext.contextManager,
        styleRegistry = resolveContext.styleRegistry,
        dataContext = resolveContext.dataContext,
        useDarkColorPalette = resolveContext.useDarkColorPalette,
    ) ?: return null

    return buildRefreshResult(
        definition = definitionWithBindings,
        presentation = presentation,
        presentationRoot = presentationRoot,
        path = presentationPath,
        resolvedComponent = resolvedComponent,
    )
}

private fun resolveSourceComponent(
    root: ComponentModel,
    locator: ComponentLocator,
): ComponentModel? {
    findComponentPath(root, locator)?.let { path ->
        return getComponentAt(root, path)
    }
    return findSourceInForTemplates(root, locator)
}

/**
 * Ищет шаблон внутри FOR, разворачивает `{#i}` и проверяет совпадение с [locator].
 */
private fun findSourceInForTemplates(
    root: ComponentModel,
    locator: ComponentLocator,
): ComponentModel? {
    if (root is LayoutModel && root.type.isForLayout()) {
        val maxForIndex = root.forParams.resolvedMaxForIndex?.toIntOrNull()
            ?: root.forParams.maxForIndex?.toIntOrNull()
            ?: return null
        val forIndexName = root.forParams.forIndexName ?: return null

        repeat(maxForIndex) { index ->
            val indexStr = index.toString()
            for (templateChild in root.children) {
                val expanded = expandComponentWithIndex(templateChild, forIndexName, indexStr)
                if (expanded.matchesLocator(locator)) return expanded
                findSourceInForTemplates(expanded, locator)?.let { return it }
            }
        }
        return null
    }

    if (root is LayoutModel) {
        for (child in root.children) {
            findSourceInForTemplates(child, locator)?.let { return it }
        }
    }
    return null
}

private fun buildRefreshResult(
    definition: ScreenDefinition,
    presentation: ScreenPresentation,
    presentationRoot: ComponentModel,
    path: List<Int>,
    resolvedComponent: ComponentModel,
): TargetedRefreshResult {
    val updatedRoot = setComponentAt(presentationRoot, path, resolvedComponent)
    return TargetedRefreshResult(
        definition = definition,
        presentation = PresentationBuilder.withUpdatedRoot(
            screenId = presentation.screenId,
            rootComponent = updatedRoot,
        ),
    )
}

private fun ComponentModel.matchesLocator(locator: ComponentLocator): Boolean =
    when (locator) {
        is ComponentLocator.Widget -> nodeCode() == locator.code
        is ComponentLocator.Layout -> this is LayoutModel && layoutCode == locator.code
    }

private fun ComponentModel.nodeCode(): String? =
    when (this) {
        is LayoutModel -> layoutCode.takeIf { it.isNotEmpty() }
        is LabelModel -> widgetCode
        is ButtonModel -> widgetCode
        is ImageModel -> widgetCode
        is InputModel -> widgetCode
        is AppBarModel -> widgetCode
        else -> null
    }

private fun findComponentPath(
    root: ComponentModel?,
    locator: ComponentLocator,
): List<Int>? {
    if (root == null) return null
    if (root.matchesLocator(locator)) return emptyList()

    if (root !is LayoutModel) return null

    for ((index, child) in root.children.withIndex()) {
        val childPath = findComponentPath(child, locator) ?: continue
        return listOf(index) + childPath
    }
    return null
}

private fun getComponentAt(root: ComponentModel, path: List<Int>): ComponentModel {
    var current = root
    for (index in path) {
        current = (current as LayoutModel).children[index]
    }
    return current
}

private fun setComponentAt(
    root: ComponentModel,
    path: List<Int>,
    newComponent: ComponentModel,
): ComponentModel {
    if (path.isEmpty()) return newComponent

    val layout = root as LayoutModel
    val childIndex = path.first()
    val updatedChild = setComponentAt(layout.children[childIndex], path.drop(1), newComponent)
    val newChildren = layout.children.toMutableList()
    newChildren[childIndex] = updatedChild
    return layout.copy(children = newChildren)
}

private fun LayoutType.isForLayout(): Boolean =
    this == LayoutType.VERTICAL_FOR || this == LayoutType.HORIZONTAL_FOR
