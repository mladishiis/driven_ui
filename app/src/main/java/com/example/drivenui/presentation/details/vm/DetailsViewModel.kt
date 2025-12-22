package com.example.drivenui.presentation.details.vm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.presentation.details.model.*
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
     * Устанавливает результат парсинга
     */
    fun setParsedResult(parsedMicroapp: SDUIParser.ParsedMicroapp?) {
        updateState {
            copy(
                parsedMicroapp = parsedMicroapp,
                hasData = parsedMicroapp != null
            )
        }
        logParsingData(parsedMicroapp)
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

            DetailsEvent.OnExportData -> {
                handleExportData()
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

                val result = uiState.value.parsedMicroapp
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
     * Создает JSON для экспорта
     */
    private fun createExportJson(result: SDUIParser.ParsedMicroapp): String {
        return buildString {
            appendLine("{")
            appendLine("  \"microapp\": {")
            result.microapp?.let {
                appendLine("    \"title\": \"${escapeJson(it.title)}\",")
                appendLine("    \"code\": \"${escapeJson(it.code)}\",")
                appendLine("    \"shortCode\": \"${escapeJson(it.shortCode)}\",")
                appendLine("    \"deeplink\": \"${escapeJson(it.deeplink)}\"")
            }
            appendLine("  },")
            appendLine("  \"statistics\": {")
            appendLine("    \"screens\": ${result.screens.size},")
            appendLine("    \"textStyles\": ${result.styles?.textStyles?.size ?: 0},")
            appendLine("    \"colorStyles\": ${result.styles?.colorStyles?.size ?: 0},")
            appendLine("    \"queries\": ${result.queries.size},")
            appendLine("    \"events\": ${result.events?.events?.size ?: 0}")
            appendLine("  }")
            appendLine("}")
        }
    }

    /**
     * Создает детализированный JSON со всеми данными парсинга
     */
    fun createDetailedJson(result: SDUIParser.ParsedMicroapp): String {
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
            appendLine("    \"layouts\": ${result.layouts.size}")
            appendLine("  },")

            // Список экранов (первые 5 для примера)
            if (result.screens.isNotEmpty()) {
                appendLine("  \"sampleScreens\": [")
                result.screens.take(5).forEachIndexed { index, screen ->
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(screen.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(screen.screenCode)}\",")
                    appendLine("      \"shortCode\": \"${escapeJson(screen.screenShortCode)}\",")
                    appendLine("      \"deeplink\": \"${escapeJson(screen.deeplink)}\",")
                    appendLine("      \"eventsCount\": ${screen.events.size},")
                    appendLine("      \"layoutsCount\": ${screen.screenLayouts.size}")
                    append("    }")
                    if (index < minOf(5, result.screens.size) - 1) append(",")
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

            // Общая информация о парсинге
            appendLine("  \"parsingInfo\": {")
            appendLine("    \"timestamp\": \"${System.currentTimeMillis()}\",")
            appendLine("    \"totalElements\": ${calculateTotalElements(result)}")
            appendLine("  }")

            appendLine("}")
        }
    }

    /**
     * Подсчитывает общее количество элементов
     */
    private fun calculateTotalElements(result: SDUIParser.ParsedMicroapp): Int {
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
    private fun logParsingData(result: SDUIParser.ParsedMicroapp?) {
        if (result == null) return

        Log.d("DetailsViewModel", "=== Данные парсинга ===")
        Log.d("DetailsViewModel", "Микроапп: ${result.microapp?.title}")
        Log.d("DetailsViewModel", "Экранов: ${result.screens.size}")
        Log.d("DetailsViewModel", "Стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("DetailsViewModel", "Стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("DetailsViewModel", "Запросов: ${result.queries.size}")
        Log.d("DetailsViewModel", "Событий: ${result.events?.events?.size ?: 0}")
        Log.d("DetailsViewModel", "Виджетов: ${result.widgets.size}")
        Log.d("DetailsViewModel", "Лэйаутов: ${result.layouts.size}")

        // Логируем первые 3 экрана
        result.screens.take(3).forEachIndexed { index, screen ->
            Log.d("DetailsViewModel", "Экран ${index + 1}: ${screen.title}")
        }

        // Логируем первые 3 запроса
        result.queries.take(3).forEachIndexed { index, query ->
            Log.d("DetailsViewModel", "Запрос ${index + 1}: ${query.title}")
        }
    }

    /**
     * Получает список экранов для отображения
     */
    fun getScreens(): List<ScreenItem> {
        return uiState.value.parsedMicroapp?.screens?.map { screen ->
            ScreenItem(
                id = screen.screenCode,
                title = screen.title,
                code = screen.screenCode,
                shortCode = screen.screenShortCode,
                deeplink = screen.deeplink,
                eventsCount = screen.events.size,
                layoutsCount = screen.screenLayouts.size
            )
        } ?: emptyList()
    }

    /**
     * Получает список стилей текста
     */
    fun getTextStyles(): List<TextStyleItem> {
        return uiState.value.parsedMicroapp?.styles?.textStyles?.map { style ->
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
        return uiState.value.parsedMicroapp?.styles?.colorStyles?.map { style ->
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
        return uiState.value.parsedMicroapp?.styles?.roundStyles?.map { style ->
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
        return uiState.value.parsedMicroapp?.styles?.paddingStyles?.map { style ->
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
        return uiState.value.parsedMicroapp?.styles?.alignmentStyles?.map { style ->
            AlignmentStyleItem(
                code = style.code
            )
        } ?: emptyList()
    }

    /**
     * Получает список запросов
     */
    fun getQueries(): List<QueryItem> {
        return uiState.value.parsedMicroapp?.queries?.map { query ->
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
        return uiState.value.parsedMicroapp?.events?.events?.map { event ->
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
        return uiState.value.parsedMicroapp?.widgets?.map { widget ->
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
        return uiState.value.parsedMicroapp?.layouts?.map { layout ->
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
        return uiState.value.parsedMicroapp?.screenQueries?.map { query ->
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
        return uiState.value.parsedMicroapp?.eventActions?.eventActions?.map { action ->
            EventActionItem(
                id = action.code,
                title = action.title,
                code = action.code,
                order = action.order,
                propertiesCount = action.properties.size
            )
        } ?: emptyList()
    }
}