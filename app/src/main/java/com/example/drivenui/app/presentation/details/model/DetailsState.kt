package com.example.drivenui.app.presentation.details.model

import com.example.drivenui.R
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.mappers.mapParsedScreenToUI
import com.example.drivenui.engine.parser.SDUIParser
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.utile.VtbState

/**
 * Состояние экрана деталей.
 *
 * @property isLoading идёт загрузка
 * @property parsedResult результат парсинга микроаппа
 * @property tabData данные для вкладок
 * @property selectedTabIndex индекс выбранной вкладки
 * @property expandedSections множество ID раскрытых секций
 * @property errorMessage сообщение об ошибке
 * @property selectedScreenComponents компоненты выбранного экрана
 */
internal data class DetailsState(
    val isLoading: Boolean = false,
    val parsedResult: SDUIParser.ParsedMicroappResult? = null,
    val tabData: DetailsTabData = DetailsTabData(),
    val selectedTabIndex: Int = 0,
    val expandedSections: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val selectedScreenComponents: List<ComponentTreeItem> = emptyList(),
) : VtbState {

    /**
     * Проверяет, есть ли данные для отображения
     */
    val hasData: Boolean get() = parsedResult != null && parsedResult.hasData()

    /**
     * Название микроаппа
     */
    val microappTitle: String get() = parsedResult?.microapp?.title ?: ""

    /**
     * Количество экранов
     */
    val screensCount: Int get() = parsedResult?.screens?.size ?: 0

    /**
     * Количество текстовых стилей
     */
    val textStylesCount: Int get() = parsedResult?.styles?.textStyles?.size ?: 0

    /**
     * Количество цветовых стилей
     */
    val colorStylesCount: Int get() = parsedResult?.styles?.colorStyles?.size ?: 0

    /**
     * Количество стилей скругления
     */
    val roundStylesCount: Int get() = parsedResult?.styles?.roundStyles?.size ?: 0

    /**
     * Количество стилей отступов
     */
    val paddingStylesCount: Int get() = parsedResult?.styles?.paddingStyles?.size ?: 0

    /**
     * Количество стилей выравнивания
     */
    val alignmentStylesCount: Int get() = parsedResult?.styles?.alignmentStyles?.size ?: 0

    /**
     * Количество запросов
     */
    val queriesCount: Int get() = parsedResult?.queries?.size ?: 0

    /**
     * Количество экранных запросов
     */
    val screenQueriesCount: Int get() = parsedResult?.screenQueries?.size ?: 0

    /**
     * Количество событий
     */
    val eventsCount: Int get() = parsedResult?.events?.events?.size ?: 0

    /**
     * Количество действий событий
     */
    val eventActionsCount: Int get() = parsedResult?.eventActions?.eventActions?.size ?: 0

    /**
     * Количество виджетов
     */
    val widgetsCount: Int get() = parsedResult?.widgets?.size ?: 0

    /**
     * Количество лэйаутов
     */
    val layoutsCount: Int get() = parsedResult?.layouts?.size ?: 0

    /**
     * Есть ли структура компонентов
     */
    val hasComponentStructure: Boolean get() =
        parsedResult?.screens?.any { it.rootComponent != null } == true

    /**
     * Общее количество компонентов
     */
    val componentsCount: Int get() = parsedResult?.countAllComponents() ?: 0

    /**
     * Количество экранов с компонентами
     */
    val screensWithComponents: Int get() =
        parsedResult?.screens?.count { it.rootComponent != null } ?: 0

    /**
     * Вкладки экрана (resource IDs для stringResource)
     */
    val tabResourceIds: List<Int> = listOf(
        R.string.tab_overview,
        R.string.tab_screens,
        R.string.tab_styles,
        R.string.tab_queries,
        R.string.tab_events,
        R.string.tab_widgets,
        R.string.tab_layouts,
        R.string.tab_details,
        R.string.tab_test,
    )

    /**
     * Получает статистику для отображения.
     *
     * @return карта со статистикой парсинга
     */
    fun getStats(): Map<String, Any> {
        return parsedResult?.getStats() ?: emptyMap()
    }

    /**
     * Получает информацию о микроаппе.
     *
     * @return информация о микроаппе или null
     */
    fun getMicroappInfo(): MicroappItem? {
        return parsedResult?.microapp?.let { microapp ->
            MicroappItem(
                id = microapp.code,
                title = microapp.title,
                code = microapp.code,
                shortCode = microapp.shortCode,
                deeplink = microapp.deeplink,
                persistents = microapp.persistents
            )
        }
    }

    /**
     * Получает информацию о стилях.
     *
     * @return сводная информация о всех стилях
     */
    fun getAllStylesInfo(): AllStylesItem {
        return AllStylesItem(
            textStylesCount = textStylesCount,
            colorStylesCount = colorStylesCount,
            roundStylesCount = roundStylesCount,
            paddingStylesCount = paddingStylesCount,
            alignmentStylesCount = alignmentStylesCount
        )
    }

    /**
     * Получает список экранов с информацией о компонентах.
     *
     * @return список экранов с подсчётом компонентов
     */
    fun getScreensWithComponents(): List<ScreenItem> {
        return parsedResult?.screens?.map { screen ->
            ScreenItem(
                id = screen.screenCode,
                title = screen.title,
                code = screen.screenCode,
                shortCode = screen.screenShortCode,
                deeplink = screen.deeplink,
                eventsCount = 0,
                layoutsCount = 0,
                hasComponents = screen.rootComponent != null,
                componentCount = screen.rootComponent?.let { countComponents(it) } ?: 0
            )
        } ?: emptyList()
    }

    /**
     * Получает компоненты экрана по коду.
     *
     * @param screenCode код экрана
     * @return дерево компонентов для указанного экрана
     */
    fun getScreenComponents(screenCode: String): List<ComponentTreeItem> {
        val screen = parsedResult?.screens?.firstOrNull { it.screenCode == screenCode }
        return screen?.rootComponent?.let { convertToTreeItems(it) } ?: emptyList()
    }

    /**
     * Получает модель компонентов первого экрана для рендеринга.
     *
     * @return модель компонентов или null при отсутствии экранов
     */
    fun getUIModelsForRender(): ComponentModel? {
        val registry = ComposeStyleRegistry(parsedResult?.styles)
        return parsedResult?.screens?.firstOrNull()?.let {
            mapParsedScreenToUI(it, registry)
        }
    }

    /**
     * Подсчитывает компоненты рекурсивно
     */
    private fun countComponents(component: Component): Int {
        var count = 1
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }

    /**
     * Конвертирует компонент в дерево для отображения
     */
    private fun convertToTreeItems(component: Component, depth: Int = 0): List<ComponentTreeItem> {
        val items = mutableListOf<ComponentTreeItem>()

        val typeName = when (component.type) {
            com.example.drivenui.engine.parser.models.ComponentType.LAYOUT -> "Layout"
            com.example.drivenui.engine.parser.models.ComponentType.WIDGET -> "Widget"
            com.example.drivenui.engine.parser.models.ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
            com.example.drivenui.engine.parser.models.ComponentType.SCREEN -> "Screen"
        }

        items.add(ComponentTreeItem(
            title = component.title,
            code = component.code,
            type = typeName,
            depth = depth,
            childrenCount = component.children.size,
            stylesCount = component.styles.size,
            eventsCount = component.events.size,
            propertiesCount = component.properties.size,
            children = emptyList()
        ))

        component.children.forEach { child ->
            items.addAll(convertToTreeItems(child, depth + 1))
        }

        return items
    }
}
