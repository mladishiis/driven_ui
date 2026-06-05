package com.example.drivenui.engine.generative_screen.refresh

import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.ForLayoutBinding
import com.example.drivenui.engine.generative_screen.models.ScreenModel
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

/**
 * Результат точечного обновления экрана.
 *
 * @property definition resolved-дерево с обновлённым узлом
 * @property source source-дерево с актуальными FOR-биндингами на пути к узлу
 */
data class TargetedRefreshResult(
    val definition: ScreenModel,
    val source: ScreenModel,
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
 * Используется только внутри [refreshComponentInScreen]: по нему строится путь
 * от корня до нужного виджета или layout, после чего перерезолвится только этот узел.
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
     * @property code [com.example.drivenui.engine.uirender.models.LabelModel.widgetCode]
     * (и аналогичное поле у других виджетов) — совпадает с `id`/`code` узла в JSON экрана
     */
    data class Widget(val code: String) : ComponentLocator

    /**
     * Поиск layout-контейнера: vertical, horizontal, layers, verticalFor и т.д.
     *
     * @property code [com.example.drivenui.engine.uirender.models.LayoutModel.layoutCode] —
     * совпадает с `id`/`code` узла type=LAYOUT в JSON экрана
     */
    data class Layout(val code: String) : ComponentLocator
}

/**
 * Перерезолвит один виджет по [widgetCode] и подставляет его в [definition].
 *
 * @param definition текущее resolved-дерево экрана
 * @param source source-дерево с шаблонами
 * @param widgetCode код виджета из JSON-файла экрана (поле id/code узла)
 * @param resolveContext контекст резолва
 * @return обновлённые экраны или null, если виджет не найден
 */
fun refreshWidgetInScreen(
    definition: ScreenModel,
    source: ScreenModel,
    widgetCode: String,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? =
    refreshComponentInScreen(
        definition = definition,
        source = source,
        locator = ComponentLocator.Widget(widgetCode),
        resolveContext = resolveContext,
    )

/**
 * Перерезолвит один layout по [layoutCode] (включая дочернее поддерево).
 *
 * @param definition текущее resolved-дерево экрана
 * @param source source-дерево с шаблонами
 * @param layoutCode код layout из JSON-файла экрана (поле id/code узла)
 * @param resolveContext контекст резолва
 * @return обновлённые экраны или null, если layout не найден
 */
fun refreshLayoutInScreen(
    definition: ScreenModel,
    source: ScreenModel,
    layoutCode: String,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? =
    refreshComponentInScreen(
        definition = definition,
        source = source,
        locator = ComponentLocator.Layout(layoutCode),
        resolveContext = resolveContext,
    )

private fun refreshComponentInScreen(
    definition: ScreenModel,
    source: ScreenModel,
    locator: ComponentLocator,
    resolveContext: RefreshResolveContext,
): TargetedRefreshResult? {
    val sourceRoot = source.rootComponent ?: return null
    val definitionRoot = definition.rootComponent ?: return null
    val path = findComponentPath(sourceRoot, locator) ?: return null

    val sourceWithForBindings = applyForBindingsOnPath(sourceRoot, path, resolveContext.dataContext)
    val sourceComponent = getComponentAt(sourceWithForBindings, path)
    val resolvedComponent = resolveComponent(
        component = sourceComponent,
        contextManager = resolveContext.contextManager,
        styleRegistry = resolveContext.styleRegistry,
        dataContext = resolveContext.dataContext,
        useDarkColorPalette = resolveContext.useDarkColorPalette,
    ) ?: return null

    return buildRefreshResult(
        definition = definition,
        source = source,
        definitionRoot = definitionRoot,
        sourceRoot = sourceWithForBindings,
        path = path,
        resolvedComponent = resolvedComponent,
    )
}

private fun buildRefreshResult(
    definition: ScreenModel,
    source: ScreenModel,
    definitionRoot: ComponentModel,
    sourceRoot: ComponentModel,
    path: List<Int>,
    resolvedComponent: ComponentModel,
): TargetedRefreshResult =
    TargetedRefreshResult(
        definition = definition.copy(
            rootComponent = setComponentAt(definitionRoot, path, resolvedComponent),
        ),
        source = source.copy(
            rootComponent = sourceRoot,
        ),
    )

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

private fun applyForBindingsOnPath(
    root: ComponentModel,
    path: List<Int>,
    dataContext: DataContext,
): ComponentModel {
    if (path.isEmpty()) return root
    if (root !is LayoutModel) return root

    val layout = resolveForLayoutBindings(root, dataContext)
    val childIndex = path.first()
    val updatedChild = applyForBindingsOnPath(layout.children[childIndex], path.drop(1), dataContext)

    if (updatedChild === layout.children[childIndex] && layout === root) {
        return root
    }

    val newChildren = layout.children.toMutableList()
    newChildren[childIndex] = updatedChild
    return layout.copy(children = newChildren)
}

private fun resolveForLayoutBindings(
    layout: LayoutModel,
    dataContext: DataContext,
): LayoutModel {
    if (!layout.type.isForLayout()) return layout
    return ForLayoutBinding.applyBindingsToComponent(layout, dataContext) as LayoutModel
}

private fun LayoutType.isForLayout(): Boolean =
    this == LayoutType.VERTICAL_FOR || this == LayoutType.HORIZONTAL_FOR
