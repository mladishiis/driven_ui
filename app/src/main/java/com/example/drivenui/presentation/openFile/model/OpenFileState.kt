package com.example.drivenui.presentation.openFile.model

import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.utile.VtbEffect
import com.example.drivenui.utile.VtbEvent
import com.example.drivenui.utile.VtbState

/** События на экране */
internal sealed interface OpenFileEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : OpenFileEvent

    /** Жмак по кнопке Загрузить файл */
    data object OnUploadFile : OpenFileEvent

    /** Показать файл */
    data object OnShowFile : OpenFileEvent

    /** Показать детали парсинга */
    data object OnShowParsingDetails : OpenFileEvent

    /** Показать тестовый экран */
    data object OnShowTestScreen : OpenFileEvent

    /** Показать статистику биндингов */
    data object OnShowBindingStats : OpenFileEvent

    /** Загрузить JSON файлы */
    data object OnLoadJsonFiles : OpenFileEvent

    /** Выбрать JSON файлы */
    data class OnSelectJsonFiles(val files: List<String>) : OpenFileEvent
}

/** События с вью-модели на экран */
internal sealed interface OpenFileEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToParsingDetails(val result: SDUIParser.ParsedMicroappResult) : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToTestScreen(val result: SDUIParser.ParsedMicroappResult) : OpenFileEffect

    /** Показать сообщение об ошибке */
    data class ShowError(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе */
    data class ShowSuccess(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе с информацией о биндингах */
    data class ShowSuccessWithBindings(
        val message: String,
        val bindingStats: Map<String, Any>?,
        val resolvedValues: Map<String, String>
    ) : OpenFileEffect

    /** Показать результат парсинга в диалоге */
    data class ShowParsingResultDialog(
        val title: String,
        val screensCount: Int,
        val textStylesCount: Int,
        val colorStylesCount: Int,
        val queriesCount: Int,
        val componentsCount: Int = 0,
        val hasComponentStructure: Boolean = false,
        val bindingsCount: Int = 0,
        val resolvedBindingsCount: Int = 0,
        val jsonFilesCount: Int = 0
    ) : OpenFileEffect

    /** Показать статистику биндингов */
    data class ShowBindingStats(
        val stats: Map<String, Any>?,
        val resolvedValues: Map<String, String>
    ) : OpenFileEffect

    /** Показать диалог выбора JSON файлов */
    data class ShowJsonFileSelectionDialog(
        val availableFiles: List<String>,
        val selectedFiles: List<String>
    ) : OpenFileEffect
}

/**
 * Состояние экрана с поддержкой новой структуры компонентов и биндингов
 *
 * @property isUploadFile состояние загрузки файла
 * @property isParsing состояние парсинга файла
 * @property parsingResult результат парсинга с новой структурой
 * @property availableFiles список доступных файлов
 * @property availableJsonFiles список доступных JSON файлов
 * @property selectedFileName выбранный файл
 * @property selectedJsonFiles выбранные JSON файлы для биндингов
 * @property errorMessage сообщение об ошибке
 * @property showJsonSelectionDialog показать диалог выбора JSON файлов
 */
internal data class OpenFileState(
    val isUploadFile: Boolean = false,
    val isParsing: Boolean = false,
    val parsingResult: SDUIParser.ParsedMicroappResult? = null,
    val availableFiles: List<String> = emptyList(),
    val availableJsonFiles: List<String> = emptyList(),
    val selectedFileName: String? = null,
    val selectedJsonFiles: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showJsonSelectionDialog: Boolean = false,
    val bindingStats: Map<String, Any>? = null,
    val resolvedValues: Map<String, String> = emptyMap()
) : VtbState {

    /**
     * Проверяет, есть ли результат парсинга для отображения
     */
    val hasParsingResult: Boolean get() = parsingResult != null

    /**
     * Проверяет, есть ли JSON файлы для выбора
     */
    val hasJsonFiles: Boolean get() = availableJsonFiles.isNotEmpty()

    /**
     * Проверяет, выбраны ли JSON файлы для биндингов
     */
    val hasSelectedJsonFiles: Boolean get() = selectedJsonFiles.isNotEmpty()

    /**
     * Получает название микроаппа
     */
    val microappTitle: String get() = parsingResult?.microapp?.title ?: "Неизвестный микроапп"

    /**
     * Получает количество экранов
     */
    val screensCount: Int get() = parsingResult?.screens?.size ?: 0

    /**
     * Получает количество текстовых стилей
     */
    val textStylesCount: Int get() = parsingResult?.styles?.textStyles?.size ?: 0

    /**
     * Получает количество цветовых стилей
     */
    val colorStylesCount: Int get() = parsingResult?.styles?.colorStyles?.size ?: 0

    /**
     * Получает количество запросов API
     */
    val queriesCount: Int get() = parsingResult?.queries?.size ?: 0

    /**
     * Получает количество событий
     */
    val eventsCount: Int get() = parsingResult?.events?.events?.size ?: 0

    /**
     * Получает количество действий событий
     */
    val eventActionsCount: Int get() = parsingResult?.eventActions?.eventActions?.size ?: 0

    /**
     * Получает количество виджетов в реестре
     */
    val widgetsCount: Int get() = parsingResult?.widgets?.size ?: 0

    /**
     * Получает количество лэйаутов в реестре
     */
    val layoutsCount: Int get() = parsingResult?.layouts?.size ?: 0

    /**
     * Получает количество биндингов
     */
    val bindingsCount: Int get() = parsingResult?.countAllBindings() ?: 0

    /**
     * Получает количество разрешенных биндингов
     */
    val resolvedBindingsCount: Int get() = parsingResult?.getResolvedValues()?.size ?: 0

    /**
     * Проверяет, есть ли контекст данных
     */
    val hasDataContext: Boolean get() = parsingResult?.dataContext != null

    /**
     * Получает общее количество компонентов во всех экранах
     */
    val componentsCount: Int get() {
        var total = 0
        parsingResult?.screens?.forEach { screen ->
            screen.rootComponent?.let { root ->
                total += countComponents(root)
            }
        }
        return total
    }

    /**
     * Проверяет, имеет ли структура компонентов (новая структура)
     */
    val hasComponentStructure: Boolean get() {
        return parsingResult?.screens?.any { it.rootComponent != null } == true
    }

    /**
     * Получает статистику парсинга для отображения
     */
    fun getParsingStats(): Map<String, Any> = buildMap {
        put("microappTitle", microappTitle)
        put("screensCount", screensCount)
        put("textStylesCount", textStylesCount)
        put("colorStylesCount", colorStylesCount)
        put("queriesCount", queriesCount)
        put("eventsCount", eventsCount)
        put("eventActionsCount", eventActionsCount)
        put("widgetsCount", widgetsCount)
        put("layoutsCount", layoutsCount)
        put("componentsCount", componentsCount)
        put("bindingsCount", bindingsCount)
        put("resolvedBindingsCount", resolvedBindingsCount)
        put("hasDataContext", hasDataContext)
        put("hasComponentStructure", hasComponentStructure)
        put("hasData", hasParsingResult)
        put("hasJsonFiles", hasJsonFiles)
        put("hasSelectedJsonFiles", hasSelectedJsonFiles)
        put("selectedJsonFilesCount", selectedJsonFiles.size)
    }

    /**
     * Получает информацию о структуре компонентов для лога
     */
    fun getComponentStructureInfo(): String {
        val builder = StringBuilder()

        parsingResult?.screens?.forEachIndexed { index, screen ->
            builder.append("Экран ${index + 1}: ${screen.title}\n")
            builder.append("  Код: ${screen.screenCode}\n")

            screen.rootComponent?.let { root ->
                val componentCount = countComponents(root)
                val maxDepth = getMaxDepth(root)
                builder.append("  Компонентов: $componentCount\n")
                builder.append("  Глубина: $maxDepth\n")
                builder.append("  Биндингов: ${countBindings(root)}\n")
                builder.append("  Типы компонентов:\n")

                val typeStats = getComponentTypeStats(root)
                typeStats.forEach { (type, count) ->
                    builder.append("    - $type: $count\n")
                }
            } ?: builder.append("  Корневой компонент: отсутствует\n")

            builder.append("\n")
        }

        return builder.toString()
    }

    /**
     * Считает количество компонентов рекурсивно
     */
    private fun countComponents(component: com.example.drivenui.parser.models.Component): Int {
        var count = 1 // текущий компонент
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }

    /**
     * Считает количество биндингов в компоненте (рекурсивно)
     */
    private fun countBindings(component: com.example.drivenui.parser.models.Component): Int {
        var count = component.properties.sumOf { it.bindings.size }
        component.children.forEach { child ->
            count += countBindings(child)
        }
        return count
    }

    /**
     * Считает количество биндингов в экране
     */
    fun countBindingsInScreen(screen: com.example.drivenui.parser.models.ParsedScreen): Int {
        return screen.rootComponent?.let { countBindings(it) } ?: 0
    }

    /**
     * Получает максимальную глубину дерева компонентов
     */
    private fun getMaxDepth(component: com.example.drivenui.parser.models.Component): Int {
        if (component.children.isEmpty()) return 1

        var maxChildDepth = 0
        component.children.forEach { child ->
            val childDepth = getMaxDepth(child)
            if (childDepth > maxChildDepth) {
                maxChildDepth = childDepth
            }
        }

        return maxChildDepth + 1
    }

    /**
     * Получает статистику по типам компонентов
     */
    private fun getComponentTypeStats(component: com.example.drivenui.parser.models.Component): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()

        fun countType(comp: com.example.drivenui.parser.models.Component) {
            val typeName = when (comp.type) {
                com.example.drivenui.parser.models.ComponentType.LAYOUT -> "Layout"
                com.example.drivenui.parser.models.ComponentType.WIDGET -> "Widget"
                com.example.drivenui.parser.models.ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
                com.example.drivenui.parser.models.ComponentType.SCREEN -> "Screen"
            }

            stats[typeName] = stats.getOrDefault(typeName, 0) + 1

            comp.children.forEach { child ->
                countType(child)
            }
        }

        countType(component)
        return stats
    }

    /**
     * Получает плоский список всех компонентов для отображения
     */
    fun getAllComponents(): List<ComponentInfo> {
        val components = mutableListOf<ComponentInfo>()

        parsingResult?.screens?.forEach { screen ->
            screen.rootComponent?.let { root ->
                collectComponents(root, 0, screen.screenCode, components)
            }
        }

        return components
    }

    /**
     * Рекурсивно собирает информацию о компонентах
     */
    private fun collectComponents(
        component: com.example.drivenui.parser.models.Component,
        depth: Int,
        screenCode: String,
        result: MutableList<ComponentInfo>
    ) {
        result.add(ComponentInfo(
            title = component.title,
            code = component.code,
            type = component.type,
            depth = depth,
            screenCode = screenCode,
            childrenCount = component.children.size,
            stylesCount = component.styles.size,
            eventsCount = component.events.size,
            propertiesCount = component.properties.size,
            bindingsCount = component.bindingProperties.size
        ))

        component.children.forEach { child ->
            collectComponents(child, depth + 1, screenCode, result)
        }
    }

    /**
     * Получает статистику биндингов
     */
    fun getBindingStatsInfo(): String {
        return if (bindingStats != null) {
            buildString {
                append("Статистика биндингов:\n")
                bindingStats.forEach { (key, value) ->
                    when (key) {
                        "resolvedValues" -> {
                            append("  $key: ${(value as Map<*, *>).size} значений\n")
                        }
                        "resolutionRate" -> {
                            val rate = value as Float
                            append("  $key: ${String.format("%.1f", rate * 100)}%\n")
                        }
                        else -> {
                            append("  $key: $value\n")
                        }
                    }
                }
                if (resolvedValues.isNotEmpty()) {
                    append("\nПримеры разрешенных значений:\n")
                    resolvedValues.entries.take(3).forEach { (key, value) ->
                        append("  $key = $value\n")
                    }
                }
            }
        } else {
            "Статистика биндингов не доступна"
        }
    }

    /**
     * Информация о компоненте для отображения
     */
    data class ComponentInfo(
        val title: String,
        val code: String,
        val type: com.example.drivenui.parser.models.ComponentType,
        val depth: Int,
        val screenCode: String,
        val childrenCount: Int,
        val stylesCount: Int,
        val eventsCount: Int,
        val propertiesCount: Int,
        val bindingsCount: Int
    ) {
        val typeName: String = when (type) {
            com.example.drivenui.parser.models.ComponentType.LAYOUT -> "Layout"
            com.example.drivenui.parser.models.ComponentType.WIDGET -> "Widget"
            com.example.drivenui.parser.models.ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
            com.example.drivenui.parser.models.ComponentType.SCREEN -> "Screen"
        }

        val indent: String = "  ".repeat(depth)
    }
}