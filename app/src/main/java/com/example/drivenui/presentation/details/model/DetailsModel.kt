package com.example.drivenui.presentation.details.model

import com.example.drivenui.parser.SDUIParserNew
import com.example.drivenui.parser.models.Component
import com.example.drivenui.utile.VtbEffect
import com.example.drivenui.utile.VtbEvent
import com.example.drivenui.utile.VtbState

/** События на экране */
internal sealed interface DetailsEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : DetailsEvent

    /** Жмак по кнопке обновления */
    data object OnRefreshClick : DetailsEvent

    /** Выбор вкладки */
    data class OnTabSelected(val tabIndex: Int) : DetailsEvent

    /** Раскрытие/сворачивание секции */
    data class OnSectionExpanded(val sectionId: String, val isExpanded: Boolean) : DetailsEvent

    /** Копирование в буфер обмена */
    data class OnCopyToClipboard(val text: String) : DetailsEvent

    /** Экспорт данных */
    data object OnExportData : DetailsEvent

    /** Показать структуру компонентов */
    data object OnShowComponentStructure : DetailsEvent

    /** Показать компоненты экрана */
    data class OnShowScreenComponents(val screenCode: String) : DetailsEvent
}

/** События с вью-модели на экран */
internal sealed interface DetailsEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : DetailsEffect

    /** Показать сообщение */
    data class ShowMessage(val message: String) : DetailsEffect

    /** Показать сообщение о копировании */
    data class ShowCopiedMessage(val text: String) : DetailsEffect

    /** Успешный экспорт */
    data class ShowExportSuccess(val filePath: String) : DetailsEffect

    /** Показать структуру компонентов */
    data class ShowComponentStructure(
        val title: String,
        val structureInfo: String
    ) : DetailsEffect

    /** Показать компоненты экрана */
    data class ShowScreenComponents(
        val screenTitle: String,
        val components: List<ComponentTreeItem>
    ) : DetailsEffect
}

/**
 * Состояние экрана деталей
 */
internal data class DetailsState(
    val isLoading: Boolean = false,
    val parsedResult: SDUIParserNew.ParsedMicroappResult? = null,
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
    val microappTitle: String get() = parsedResult?.microapp?.title ?: "Детали парсинга"

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
     * Вкладки экрана
     */
    val tabs: List<String> = listOf(
        "Обзор",
        "Экраны",
        "Стили",
        "Запросы",
        "События",
        "Виджеты",
        "Лэйауты",
        "Детали"
    )

    /**
     * Получает статистику для отображения
     */
    fun getStats(): Map<String, Any> {
        return parsedResult?.getStats() ?: emptyMap()
    }

    /**
     * Получает информацию о микроаппе
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
     * Получает информацию о стилях
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
     * Получает список экранов с информацией о компонентах
     */
    fun getScreensWithComponents(): List<ScreenItem> {
        return parsedResult?.screens?.map { screen ->
            ScreenItem(
                id = screen.screenCode,
                title = screen.title,
                code = screen.screenCode,
                shortCode = screen.screenShortCode,
                deeplink = screen.deeplink,
                eventsCount = 0, // TODO: Получить реальное количество событий
                layoutsCount = 0, // TODO: Получить реальное количество лэйаутов
                hasComponents = screen.rootComponent != null,
                componentCount = screen.rootComponent?.let { countComponents(it) } ?: 0
            )
        } ?: emptyList()
    }

    /**
     * Получает компоненты экрана по коду
     */
    fun getScreenComponents(screenCode: String): List<ComponentTreeItem> {
        val screen = parsedResult?.screens?.firstOrNull { it.screenCode == screenCode }
        return screen?.rootComponent?.let { convertToTreeItems(it) } ?: emptyList()
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
            com.example.drivenui.parser.models.ComponentType.LAYOUT -> "Layout"
            com.example.drivenui.parser.models.ComponentType.WIDGET -> "Widget"
            com.example.drivenui.parser.models.ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
            com.example.drivenui.parser.models.ComponentType.SCREEN -> "Screen"
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
            children = emptyList() // Дети будут добавлены отдельно
        ))

        component.children.forEach { child ->
            items.addAll(convertToTreeItems(child, depth + 1))
        }

        return items
    }
}

/**
 * Модель для отображения экрана с поддержкой компонентов
 */
data class ScreenItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val eventsCount: Int,
    val layoutsCount: Int,
    val hasComponents: Boolean = false,
    val componentCount: Int = 0
)

/**
 * Модель для отображения стиля текста
 */
data class TextStyleItem(
    val code: String,
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: Int
)

/**
 * Модель для отображения стиля цвета
 */
data class ColorStyleItem(
    val code: String,
    val lightColor: String,
    val darkColor: String,
    val lightOpacity: Int,
    val darkOpacity: Int
)

/**
 * Модель для отображения стиля скругления
 */
data class RoundStyleItem(
    val code: String,
    val radiusValue: Int
)

/**
 * Модель для отображения стиля отступа
 */
data class PaddingStyleItem(
    val code: String,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int
)

/**
 * Модель для отображения стиля выравнивания
 */
data class AlignmentStyleItem(
    val code: String
)

/**
 * Модель для отображения запроса
 */
data class QueryItem(
    val title: String,
    val code: String,
    val type: String,
    val endpoint: String,
    val propertiesCount: Int
)

/**
 * Модель для отображения экранного запроса
 */
data class ScreenQueryItem(
    val id: String,
    val code: String,
    val screenCode: String,
    val queryCode: String,
    val order: Int
)

/**
 * Модель для отображения события
 */
data class EventItem(
    val title: String,
    val code: String,
    val actionsCount: Int
)

/**
 * Модель для отображения действия события
 */
data class EventActionItem(
    val id: String,
    val title: String,
    val code: String,
    val order: Int,
    val propertiesCount: Int
)

/**
 * Модель для отображения виджета
 */
data class WidgetItem(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val propertiesCount: Int,
    val stylesCount: Int,
    val eventsCount: Int
)

/**
 * Модель для отображения лэйаута
 */
data class LayoutItem(
    val id: String,
    val title: String,
    val code: String,
    val propertiesCount: Int
)

/**
 * Модель для отображения микроаппа
 */
data class MicroappItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val persistents: List<String>
)

/**
 * Модель для отображения всех стилей
 */
data class AllStylesItem(
    val textStylesCount: Int,
    val colorStylesCount: Int,
    val roundStylesCount: Int,
    val paddingStylesCount: Int,
    val alignmentStylesCount: Int
)

/**
 * Модель для отображения элемента дерева компонентов
 */
data class ComponentTreeItem(
    val title: String,
    val code: String,
    val type: String,
    val depth: Int = 0,
    val childrenCount: Int = 0,
    val stylesCount: Int = 0,
    val eventsCount: Int = 0,
    val propertiesCount: Int = 0,
    val children: List<ComponentTreeItem> = emptyList()
) {
    /**
     * Получает отступ для отображения
     */
    val indent: String get() = "  ".repeat(depth)

    /**
     * Проверяет, имеет ли элемент детей
     */
    val hasChildren: Boolean get() = children.isNotEmpty() || childrenCount > 0

    /**
     * Получает информацию для отображения
     */
    val displayInfo: String get() {
        return buildString {
            append("$type: $title")
            if (propertiesCount > 0) append(" (свойств: $propertiesCount)")
            if (stylesCount > 0) append(", стилей: $stylesCount")
            if (eventsCount > 0) append(", событий: $eventsCount")
            if (childrenCount > 0) append(", детей: $childrenCount")
        }
    }
}