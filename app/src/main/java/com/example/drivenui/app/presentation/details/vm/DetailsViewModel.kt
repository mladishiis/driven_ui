package com.example.drivenui.app.presentation.details.vm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.parser.SDUIParser
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.ComponentType
import com.example.drivenui.engine.parser.models.Microapp
import com.example.drivenui.app.presentation.details.model.AlignmentStyleItem
import com.example.drivenui.app.presentation.details.model.ColorStyleItem
import com.example.drivenui.app.presentation.details.model.ComponentTreeItem
import com.example.drivenui.app.presentation.details.model.DetailsEffect
import com.example.drivenui.app.presentation.details.model.DetailsEvent
import com.example.drivenui.app.presentation.details.model.DetailsState
import com.example.drivenui.app.presentation.details.model.EventActionItem
import com.example.drivenui.app.presentation.details.model.EventItem
import com.example.drivenui.app.presentation.details.model.LayoutItem
import com.example.drivenui.app.presentation.details.model.PaddingStyleItem
import com.example.drivenui.app.presentation.details.model.QueryItem
import com.example.drivenui.app.presentation.details.model.RoundStyleItem
import com.example.drivenui.app.presentation.details.model.ScreenItem
import com.example.drivenui.app.presentation.details.model.ScreenQueryItem
import com.example.drivenui.app.presentation.details.model.TextStyleItem
import com.example.drivenui.app.presentation.details.model.WidgetItem
import com.example.drivenui.utile.CoreMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DetailsViewModel @Inject constructor(
    private val context: Context
) : CoreMviViewModel<DetailsEvent, DetailsState, DetailsEffect>() {

    override fun createInitialState() = DetailsState()

    /**
     * Устанавливает результат парсинга с новой структурой
     */
    fun setParsedResult(parsedResult: SDUIParser.ParsedMicroappResult?) {
        updateState {
            copy(
                parsedResult = parsedResult
            )
        }
        logParsingData(parsedResult)
    }

    override fun handleEvent(event: DetailsEvent) {
        when (event) {
            DetailsEvent.OnBackClick -> {
                setEffect { DetailsEffect.GoBack }
            }

            DetailsEvent.OnRefreshClick -> {
                handleRefresh()
            }

            is DetailsEvent.OnTabSelected -> {
                updateState { copy(selectedTabIndex = event.tabIndex) }
            }

            is DetailsEvent.OnSectionExpanded -> {
                handleSectionExpanded(event.sectionId, event.isExpanded)
            }

            is DetailsEvent.OnCopyToClipboard -> {
                handleCopyToClipboard(event.text)
            }

            is DetailsEvent.OnShowScreenComponents -> {
                handleShowScreenComponents(event.screenCode)
            }

            DetailsEvent.OnExportData -> {
                handleExportData()
            }

            DetailsEvent.OnShowComponentStructure -> {
                handleShowComponentStructure()
            }
        }
    }

    /**
     * Обработка обновления данных
     */
    private fun handleRefresh() {
        viewModelScope.launch {
            try {
                updateState { copy(isLoading = true) }
                // Здесь можно добавить логику обновления данных
                Thread.sleep(500) // Имитация загрузки
                updateState { copy(isLoading = false) }
                setEffect { DetailsEffect.ShowMessage("Данные обновлены") }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = "Ошибка обновления: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Обработка разворачивания/сворачивания секций
     */
    private fun handleSectionExpanded(sectionId: String, isExpanded: Boolean) {
        val newExpandedSections = if (isExpanded) {
            uiState.value.expandedSections + sectionId
        } else {
            uiState.value.expandedSections - sectionId
        }
        updateState { copy(expandedSections = newExpandedSections) }
    }

    /**
     * Копирование текста в буфер обмена
     */
    private fun handleCopyToClipboard(text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SDUI Data", text)
            clipboard.setPrimaryClip(clip)
            setEffect { DetailsEffect.ShowCopiedMessage("Скопировано: ${text.take(30)}...") }
        } catch (e: Exception) {
            Log.e("DetailsViewModel", "Ошибка копирования в буфер", e)
            setEffect { DetailsEffect.ShowMessage("Ошибка копирования: ${e.message}") }
        }
    }

    /**
     * Экспорт данных
     */
    private fun handleExportData() {
        viewModelScope.launch {
            try {
                updateState { copy(isLoading = true) }

                val result = uiState.value.parsedResult
                if (result == null) {
                    setEffect { DetailsEffect.ShowMessage("Нет данных для экспорта") }
                    return@launch
                }

                // Создаем JSON для экспорта
                val jsonData = createDetailedJson(result)

                // Сохраняем в файл
                val fileName = "sdui_export_${System.currentTimeMillis()}.json"
                val file = context.filesDir.resolve(fileName)
                file.writeText(jsonData)

                updateState { copy(isLoading = false) }
                setEffect {
                    DetailsEffect.ShowExportSuccess("Файл сохранен: ${file.absolutePath}")
                }

            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                setEffect { DetailsEffect.ShowMessage("Ошибка экспорта: ${e.message}") }
            }
        }
    }

    /**
     * Показать структуру компонентов
     */
    private fun handleShowComponentStructure() {
        val result = uiState.value.parsedResult
        if (result != null && result.screens.isNotEmpty()) {
            val structureInfo = buildString {
                appendLine("=== Структура компонентов ===")
                result.screens.forEachIndexed { index, screen ->
                    appendLine("Экран ${index + 1}: ${screen.title} (${screen.screenCode})")
                    screen.rootComponent?.let { root ->
                        val componentCount = countComponents(root)
                        val maxDepth = getMaxDepth(root)
                        appendLine("  Компонентов: $componentCount")
                        appendLine("  Макс. глубина: $maxDepth")
                        appendLine("  Типы компонентов:")

                        val typeStats = getComponentTypeStats(root)
                        typeStats.forEach { (type, count) ->
                            appendLine("    - $type: $count")
                        }

                        // Логируем первые 3 уровня дерева
                        appendLine("  Дерево компонентов:")
                        logComponentTree(root, 0, 3, this)
                    } ?: appendLine("  Корневой компонент: отсутствует")
                    appendLine()
                }
            }

            setEffect {
                DetailsEffect.ShowComponentStructure(
                    title = "Структура компонентов",
                    structureInfo = structureInfo
                )
            }
        }
    }

    /**
     * Создает детализированный JSON со всеми данными парсинга
     */
    private fun createDetailedJson(result: SDUIParser.ParsedMicroappResult): String {
        return buildString {
            appendLine("{")

            // Микроапп
            result.microapp?.let {
                appendLine("  \"microapp\": {")
                appendLine("    \"title\": \"${escapeJson(it.title)}\",")
                appendLine("    \"code\": \"${escapeJson(it.code)}\",")
                appendLine("    \"shortCode\": \"${escapeJson(it.shortCode)}\",")
                appendLine("    \"deeplink\": \"${escapeJson(it.deeplink)}\",")
                appendLine("    \"persistents\": [")
                it.persistents.forEachIndexed { index, persistent ->
                    append("      \"${escapeJson(persistent)}\"")
                    if (index < it.persistents.size - 1) append(",")
                    appendLine()
                }
                appendLine("    ]")
                appendLine("  },")
            }

            // Статистика
            appendLine("  \"statistics\": {")
            appendLine("    \"screens\": ${result.screens.size},")
            appendLine("    \"textStyles\": ${result.styles?.textStyles?.size ?: 0},")
            appendLine("    \"colorStyles\": ${result.styles?.colorStyles?.size ?: 0},")
            appendLine("    \"roundStyles\": ${result.styles?.roundStyles?.size ?: 0},")
            appendLine("    \"paddingStyles\": ${result.styles?.paddingStyles?.size ?: 0},")
            appendLine("    \"alignmentStyles\": ${result.styles?.alignmentStyles?.size ?: 0},")
            appendLine("    \"queries\": ${result.queries.size},")
            appendLine("    \"screenQueries\": ${result.screenQueries.size},")
            appendLine("    \"events\": ${result.events?.events?.size ?: 0},")
            appendLine("    \"eventActions\": ${result.eventActions?.eventActions?.size ?: 0},")
            appendLine("    \"widgets\": ${result.widgets.size},")
            appendLine("    \"layouts\": ${result.layouts.size},")
            appendLine("    \"componentsCount\": ${result.countAllComponents()}")
            appendLine("  },")

            // Список экранов
            if (result.screens.isNotEmpty()) {
                appendLine("  \"screens\": [")
                result.screens.forEachIndexed { index, screen ->
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(screen.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(screen.screenCode)}\",")
                    appendLine("      \"shortCode\": \"${escapeJson(screen.screenShortCode)}\",")
                    appendLine("      \"deeplink\": \"${escapeJson(screen.deeplink)}\",")
                    appendLine("      \"hasComponentStructure\": ${screen.rootComponent != null}")
                    screen.rootComponent?.let { root ->
                        appendLine("      \"componentsCount\": ${countComponents(root)}")
                    }
                    append("    }")
                    if (index < result.screens.size - 1) append(",")
                    appendLine()
                }
                appendLine("  ],")
            }

            // Список запросов (первые 5 для примера)
            if (result.queries.isNotEmpty()) {
                appendLine("  \"sampleQueries\": [")
                result.queries.take(5).forEachIndexed { index, query ->
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(query.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(query.code)}\",")
                    appendLine("      \"type\": \"${escapeJson(query.type)}\",")
                    appendLine("      \"endpoint\": \"${escapeJson(query.endpoint)}\",")
                    appendLine("      \"propertiesCount\": ${query.properties.size}")
                    append("    }")
                    if (index < minOf(5, result.queries.size) - 1) append(",")
                    appendLine()
                }
                appendLine("  ],")
            }

            // Компонентная структура
            if (result.screens.any { it.rootComponent != null }) {
                appendLine("  \"componentStructure\": {")
                appendLine("    \"totalComponents\": ${result.countAllComponents()},")
                appendLine("    \"screensWithComponents\": ${result.screens.count { it.rootComponent != null }},")
                appendLine("    \"sampleComponentTree\": \"Показать полную структуру в UI\"")
                appendLine("  },")
            }

            // Общая информация о парсинге
            appendLine("  \"parsingInfo\": {")
            appendLine("    \"timestamp\": \"${System.currentTimeMillis()}\",")
            appendLine("    \"parserVersion\": \"new\",")
            appendLine("    \"totalElements\": ${calculateTotalElements(result)},")
            appendLine("    \"hasData\": ${uiState.value.hasData}")
            appendLine("  }")

            appendLine("}")
        }
    }

    /**
     * Подсчитывает общее количество элементов
     */
    private fun calculateTotalElements(result: SDUIParser.ParsedMicroappResult): Int {
        var total = 0
        total += result.screens.size
        total += result.styles?.textStyles?.size ?: 0
        total += result.styles?.colorStyles?.size ?: 0
        total += result.styles?.roundStyles?.size ?: 0
        total += result.styles?.paddingStyles?.size ?: 0
        total += result.styles?.alignmentStyles?.size ?: 0
        total += result.queries.size
        total += result.screenQueries.size
        total += result.events?.events?.size ?: 0
        total += result.eventActions?.eventActions?.size ?: 0
        total += result.widgets.size
        total += result.layouts.size
        total += result.countAllComponents()
        return total
    }

    /**
     * Экранирует специальные символы для JSON
     */
    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Логирует данные парсинга
     */
    private fun logParsingData(result: SDUIParser.ParsedMicroappResult?) {
        if (result == null) return

        Log.d("DetailsViewModel", "=== Данные парсинга (новая структура) ===")
        Log.d("DetailsViewModel", "Микроапп: ${result.microapp?.title}")
        Log.d("DetailsViewModel", "Экранов: ${result.screens.size}")
        Log.d("DetailsViewModel", "Стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("DetailsViewModel", "Стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("DetailsViewModel", "Запросов: ${result.queries.size}")
        Log.d("DetailsViewModel", "Событий: ${result.events?.events?.size ?: 0}")
        Log.d("DetailsViewModel", "Виджетов: ${result.widgets.size}")
        Log.d("DetailsViewModel", "Лэйаутов: ${result.layouts.size}")
        Log.d("DetailsViewModel", "Компонентов всего: ${result.countAllComponents()}")

        // Логируем первые 3 экрана
        result.screens.take(3).forEachIndexed { index, screen ->
            Log.d("DetailsViewModel", "Экран ${index + 1}: ${screen.title}")
            screen.rootComponent?.let { root ->
                Log.d("DetailsViewModel", "  Компонентов: ${countComponents(root)}")
            }
        }

        // Логируем структуру компонентов первого экрана
        result.screens.firstOrNull()?.rootComponent?.let { root ->
            Log.d("DetailsViewModel", "Структура компонентов первого экрана:")
            logComponentTree(root, 0, 2)
        }
    }

    /**
     * Рекурсивно логирует дерево компонентов
     */
    private fun logComponentTree(
        component: Component,
        depth: Int,
        maxDepth: Int = Int.MAX_VALUE,
        builder: StringBuilder? = null
    ) {
        if (depth > maxDepth) return

        val indent = "  ".repeat(depth)
        val message = "$indent${component.type}: ${component.title} (${component.code})"

        if (builder != null) {
            builder.appendLine(message)
        } else {
            Log.d("DetailsViewModel", message)
        }

        if (depth < maxDepth) {
            component.children.forEach { child ->
                logComponentTree(child, depth + 1, maxDepth, builder)
            }
        } else if (component.children.isNotEmpty()) {
            val skipMessage = "$indent  ... ещё ${component.children.size} компонентов"
            if (builder != null) {
                builder.appendLine(skipMessage)
            } else {
                Log.d("DetailsViewModel", skipMessage)
            }
        }
    }

    /**
     * Считает количество компонентов рекурсивно
     */
    private fun countComponents(component: Component): Int {
        var count = 1
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }

    /**
     * Получает максимальную глубину дерева
     */
    private fun getMaxDepth(component: Component): Int {
        if (component.children.isEmpty()) return 1

        var maxDepth = 0
        component.children.forEach { child ->
            val childDepth = getMaxDepth(child)
            if (childDepth > maxDepth) {
                maxDepth = childDepth
            }
        }

        return maxDepth + 1
    }

    /**
     * Получает статистику по типам компонентов
     */
    private fun getComponentTypeStats(component: Component): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()

        fun countType(comp: Component) {
            val typeName = when (comp.type) {
                ComponentType.LAYOUT -> "Layout"
                ComponentType.WIDGET -> "Widget"
                ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
                ComponentType.SCREEN -> "Screen"
            }

            stats[typeName] = stats.getOrDefault(typeName, 0) + 1

            comp.children.forEach { child ->
                countType(child)
            }
        }

        countType(component)
        return stats
    }

    // ===== Методы для получения данных для UI =====

    /**
     * Получает статистику для отображения
     */
    fun getStats(): Map<String, Any> {
        val result = uiState.value.parsedResult
        return result?.getStats() ?: emptyMap()
    }

    /**
     * Получает микроапп
     */
    fun getMicroapp(): Microapp? = uiState.value.parsedResult?.microapp

    /**
     * Получает список экранов для отображения
     */
    fun getScreens(): List<ScreenItem> {
        return uiState.value.getScreensWithComponents()
    }

    /**
     * Получает список стилей текста
     */
    fun getTextStyles(): List<TextStyleItem> {
        return uiState.value.parsedResult?.styles?.textStyles?.map { style ->
            TextStyleItem(
                code = style.code,
                fontFamily = style.fontFamily,
                fontSize = style.fontSize,
                fontWeight = style.fontWeight
            )
        } ?: emptyList()
    }

    /**
     * Получает список стилей цвета
     */
    fun getColorStyles(): List<ColorStyleItem> {
        return uiState.value.parsedResult?.styles?.colorStyles?.map { style ->
            ColorStyleItem(
                code = style.code,
                lightColor = style.lightTheme.color,
                darkColor = style.darkTheme.color,
                lightOpacity = style.lightTheme.opacity,
                darkOpacity = style.darkTheme.opacity
            )
        } ?: emptyList()
    }

    /**
     * Получает список стилей скругления
     */
    fun getRoundStyles(): List<RoundStyleItem> {
        return uiState.value.parsedResult?.styles?.roundStyles?.map { style ->
            RoundStyleItem(
                code = style.code,
                radiusValue = style.radiusValue
            )
        } ?: emptyList()
    }

    /**
     * Получает список стилей отступов
     */
    fun getPaddingStyles(): List<PaddingStyleItem> {
        return uiState.value.parsedResult?.styles?.paddingStyles?.map { style ->
            PaddingStyleItem(
                code = style.code,
                paddingLeft = style.paddingLeft,
                paddingTop = style.paddingTop,
                paddingRight = style.paddingRight,
                paddingBottom = style.paddingBottom
            )
        } ?: emptyList()
    }

    /**
     * Получает список стилей выравнивания
     */
    fun getAlignmentStyles(): List<AlignmentStyleItem> {
        return uiState.value.parsedResult?.styles?.alignmentStyles?.map { style ->
            AlignmentStyleItem(
                code = style.code
            )
        } ?: emptyList()
    }

    /**
     * Получает список запросов
     */
    fun getQueries(): List<QueryItem> {
        return uiState.value.parsedResult?.queries?.map { query ->
            QueryItem(
                title = query.title,
                code = query.code,
                type = query.type,
                endpoint = query.endpoint,
                propertiesCount = query.properties.size
            )
        } ?: emptyList()
    }

    /**
     * Получает список событий
     */
    fun getEvents(): List<EventItem> {
        return uiState.value.parsedResult?.events?.events?.map { event ->
            EventItem(
                title = event.title,
                code = event.code,
                actionsCount = event.eventActions.size
            )
        } ?: emptyList()
    }

    /**
     * Получает список виджетов
     */
    fun getWidgets(): List<WidgetItem> {
        return uiState.value.parsedResult?.widgets?.map { widget ->
            WidgetItem(
                id = widget.code,
                title = widget.title,
                code = widget.code,
                type = widget.type,
                propertiesCount = widget.properties.size,
                stylesCount = widget.styles.size,
                eventsCount = widget.events.size
            )
        } ?: emptyList()
    }

    /**
     * Получает список лэйаутов
     */
    fun getLayouts(): List<LayoutItem> {
        return uiState.value.parsedResult?.layouts?.map { layout ->
            LayoutItem(
                id = layout.code,
                title = layout.title,
                code = layout.code,
                propertiesCount = layout.properties.size
            )
        } ?: emptyList()
    }

    /**
     * Получает список экранных запросов
     */
    fun getScreenQueries(): List<ScreenQueryItem> {
        return uiState.value.parsedResult?.screenQueries?.map { query ->
            ScreenQueryItem(
                id = query.code,
                code = query.code,
                screenCode = query.screenCode,
                queryCode = query.queryCode,
                order = query.order
            )
        } ?: emptyList()
    }

    /**
     * Получает список действий событий
     */
    fun getEventActions(): List<EventActionItem> {
        return uiState.value.parsedResult?.eventActions?.eventActions?.map { action ->
            EventActionItem(
                id = action.code,
                title = action.title,
                code = action.code,
                order = action.order,
                propertiesCount = action.properties.size
            )
        } ?: emptyList()
    }

    /**
     * Получает структуру компонентов для экрана
     */
    fun getComponentStructure(screenCode: String): List<ComponentTreeItem> {
        return uiState.value.getScreenComponents(screenCode)
    }

    /**
     * Получает структуру компонентов для экрана TEST
     */
    fun getComponentModelForRender(): ComponentModel? {
        return uiState.value.getUIModelsForRender()
    }

    /**
     * Обработка показа компонентов экрана
     */
    private fun handleShowScreenComponents(screenCode: String) {
        val components = getComponentStructure(screenCode)
        if (components.isNotEmpty()) {
            val screenTitle = uiState.value.parsedResult?.screens
                ?.firstOrNull { it.screenCode == screenCode }?.title ?: screenCode

            setEffect {
                DetailsEffect.ShowScreenComponents(
                    screenTitle = screenTitle,
                    components = components
                )
            }
        } else {
            setEffect {
                DetailsEffect.ShowMessage("У экрана нет компонентов")
            }
        }
    }
}