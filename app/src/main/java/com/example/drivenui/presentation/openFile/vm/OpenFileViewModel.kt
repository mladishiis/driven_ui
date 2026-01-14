package com.example.drivenui.presentation.openFile.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.domain.FileInteractor
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.model.OpenFileState
import com.example.drivenui.utile.CoreMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
internal class OpenFileViewModel @Inject constructor(
    private val interactor: FileInteractor
) : CoreMviViewModel<OpenFileEvent, OpenFileState, OpenFileEffect>() {

    init {
        // Автоматически загружаем JSON файлы при создании ViewModel
        loadJsonFilesOnInit()
    }

    override fun createInitialState() = OpenFileState()

    override fun handleEvent(event: OpenFileEvent) {
        when (event) {
            OpenFileEvent.OnBackClick -> {
                setEffect { OpenFileEffect.GoBack }
            }

            OpenFileEvent.OnUploadFile -> {
                handleUploadFile()
            }

            OpenFileEvent.OnShowFile -> {
                handleShowFile()
            }

            OpenFileEvent.OnShowParsingDetails -> {
                handleShowParsingDetails()
            }

            OpenFileEvent.OnShowTestScreen -> {
                handleShowTestScreen()
            }

            OpenFileEvent.OnShowBindingStats -> {
                handleShowBindingStats()
            }

            OpenFileEvent.OnLoadJsonFiles -> {
                handleLoadJsonFiles()
            }

            is OpenFileEvent.OnSelectJsonFiles -> {
                handleSelectJsonFiles(event.files)
            }
        }
    }

    private fun loadJsonFilesOnInit() {
        viewModelScope.launch {
            try {
                val jsonFiles = interactor.getAvailableJsonFiles()
                updateState { copy(availableJsonFiles = jsonFiles) }

                // Автоматически выбираем первые 2 JSON файла (или все, если меньше)
                val selectedFiles = if (jsonFiles.size >= 2) {
                    jsonFiles.take(2)
                } else {
                    jsonFiles
                }

                if (selectedFiles.isNotEmpty()) {
                    updateState { copy(selectedJsonFiles = selectedFiles) }
                    Log.d("OpenFileViewModel", "Автоматически выбраны JSON файлы: ${selectedFiles.joinToString(", ")}")
                }
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON файлов", e)
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг файла
     */
    private fun handleUploadFile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Устанавливаем состояние загрузки
                withContext(Dispatchers.Main) {
                    updateState { copy(isUploadFile = true, isParsing = true, errorMessage = null) }
                }

                // Получаем доступные файлы
                val files = interactor.getAvailableFiles()
                if (files.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        updateState {
                            copy(
                                isUploadFile = false,
                                isParsing = false,
                                errorMessage = "Файлы не найдены в assets"
                            )
                        }
                        setEffect { OpenFileEffect.ShowError("Файлы не найдены в assets") }
                    }
                    return@launch
                }

                Log.d("OpenFileViewModel", "Найдены файлы: ${files.joinToString(", ")}")

                // Выбираем первый XML файл
                val fileName = files.firstOrNull { it.endsWith(".xml") } ?: files.first()

                withContext(Dispatchers.Main) {
                    updateState { copy(selectedFileName = fileName) }
                }

                Log.d("OpenFileViewModel", "Начинаем парсинг файла: $fileName")

                // Загружаем и парсим файл с новой структурой
                val parsedResult = interactor.parseFileFromAssets(fileName)

                Log.d("OpenFileViewModel", "Результат парсинга: ${parsedResult.screens.size} экранов")

                // Сохраняем результат
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            parsingResult = parsedResult,
                            errorMessage = null
                        )
                    }
                }

                // Логируем результат
                logParsingResult(parsedResult)

                // Создаем сообщение об успехе
                val successMessage = buildString {
                    append("Файл '$fileName' успешно спарсен!\n")
                    append("Результат:\n")
                    parsedResult.microapp?.let { append("• Микроапп: ${it.title}\n") }
                    append("• Экран${if (parsedResult.screens.size != 1) "ов" else ""}: ${parsedResult.screens.size}\n")
                    append("• Биндинги: ${parsedResult.countAllBindings()}\n")

                    // Логируем структуру компонентов
                    parsedResult.screens.forEachIndexed { index, screen ->
                        append("  Экран ${index + 1}: ${screen.title}\n")
                        screen.rootComponent?.let { root ->
                            val componentCount = countComponents(root)
                            append("    Компонентов: $componentCount\n")
                        }
                    }

                    append("• Стили текста: ${parsedResult.styles?.textStyles?.size ?: 0}\n")
                    append("• Стили цвета: ${parsedResult.styles?.colorStyles?.size ?: 0}\n")
                    append("• Запросов API: ${parsedResult.queries.size}")
                }

                withContext(Dispatchers.Main) {
                    setEffect { OpenFileEffect.ShowSuccess(successMessage) }
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при парсинге файла", e)

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = "Ошибка: ${e.localizedMessage}"
                        )
                    }

                    setEffect {
                        OpenFileEffect.ShowError("Ошибка при парсинге: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    /**
     * Загружает список JSON файлов
     */
    private fun handleLoadJsonFiles() {
        viewModelScope.launch {
            try {
                val jsonFiles = interactor.getAvailableJsonFiles()
                Log.d("OpenFileViewModel", "Загружены JSON файлы: ${jsonFiles.joinToString(", ")}")

                updateState { copy(availableJsonFiles = jsonFiles) }

                if (jsonFiles.isEmpty()) {
                    setEffect { OpenFileEffect.ShowError("JSON файлы не найдены в assets") }
                } else {
                    setEffect { OpenFileEffect.ShowSuccess("Найдено ${jsonFiles.size} JSON файлов") }
                }
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON файлов", e)
                setEffect { OpenFileEffect.ShowError("Ошибка при загрузке JSON файлов") }
            }
        }
    }

    /**
     * Обрабатывает выбор JSON файлов
     */
    private fun handleSelectJsonFiles(files: List<String>) {
        updateState { copy(selectedJsonFiles = files) }

        // Показываем диалог выбора
        setEffect {
            OpenFileEffect.ShowJsonFileSelectionDialog(
                availableFiles = uiState.value.availableJsonFiles,
                selectedFiles = files
            )
        }
    }

    /**
     * Показывает статистику по биндингам
     */
    private fun handleShowBindingStats() {
        val currentResult = uiState.value.parsingResult
        if (currentResult != null) {
            val bindingStats = uiState.value.bindingStats ?: interactor.getBindingStats()
            val resolvedValues = uiState.value.resolvedValues

            setEffect {
                OpenFileEffect.ShowBindingStats(
                    stats = bindingStats,
                    resolvedValues = resolvedValues
                )
            }
        } else {
            setEffect { OpenFileEffect.ShowError("Сначала загрузите файл с биндингами") }
        }
    }

    /**
     * Считает количество компонентов в дереве (рекурсивно)
     */
    private fun countComponents(component: com.example.drivenui.parser.models.Component?): Int {
        if (component == null) return 0

        var count = 1 // текущий компонент
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }

    /**
     * Показывает последний спарсенный файл
     */
    private fun handleShowFile() {
        val currentResult = uiState.value.parsingResult
        if (currentResult != null) {
            setEffect { OpenFileEffect.NavigateToParsingDetails(currentResult) }
        } else {
            setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
        }
    }

    /**
     * Показывает детали парсинга
     */
    private fun handleShowParsingDetails() {
        val currentResult = uiState.value.parsingResult
        if (currentResult != null) {
            setEffect { OpenFileEffect.NavigateToParsingDetails(currentResult) }
        } else {
            setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
        }
    }

    /**
     * Показывает тестовый экран
     */
    private fun handleShowTestScreen() {
        val currentResult = uiState.value.parsingResult
        if (currentResult != null) {
            setEffect { OpenFileEffect.NavigateToTestScreen(currentResult) }
        } else {
            setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
        }
    }

    /**
     * Логирует результат парсинга с новой структурой
     */
    private fun logParsingResult(result: SDUIParser.ParsedMicroappResult) {
        Log.d("OpenFileViewModel", "=== Результат парсинга (новая структура) ===")
        Log.d("OpenFileViewModel", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("OpenFileViewModel", "Код: ${result.microapp?.code ?: "Не указан"}")
        Log.d("OpenFileViewModel", "Deeplink: ${result.microapp?.deeplink ?: "Не указан"}")
        Log.d("OpenFileViewModel", "Экранов: ${result.screens.size}")
        Log.d("OpenFileViewModel", "Всего биндингов: ${result.countAllBindings()}")

        result.screens.forEachIndexed { index, screen ->
            Log.d("OpenFileViewModel", "  Экран ${index + 1}: ${screen.title}")
            Log.d("OpenFileViewModel", "    Код: ${screen.screenCode}")
            Log.d("OpenFileViewModel", "    Deeplink: ${screen.deeplink}")

            screen.rootComponent?.let { root ->
                val componentCount = countComponents(root)
                Log.d("OpenFileViewModel", "    Компонентов в дереве: $componentCount")
                logComponentStructure(root, "      ")
            }
        }

        Log.d("OpenFileViewModel", "Стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("OpenFileViewModel", "Стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("OpenFileViewModel", "Запросов API: ${result.queries.size}")
        Log.d("OpenFileViewModel", "Экранных запросов: ${result.screenQueries.size}")
        Log.d("OpenFileViewModel", "Виджетов в реестре: ${result.widgets.size}")
        Log.d("OpenFileViewModel", "Лэйаутов в реестре: ${result.layouts.size}")

        // Информация о биндингах
        val resolvedValues = result.getResolvedValues()
        Log.d("OpenFileViewModel", "Разрешено биндингов: ${resolvedValues.size}")
        if (resolvedValues.isNotEmpty()) {
            Log.d("OpenFileViewModel", "Примеры разрешенных значений:")
            resolvedValues.entries.take(3).forEach { (key, value) ->
                Log.d("OpenFileViewModel", "  $key = $value")
            }
        }

        Log.d("OpenFileViewModel", "=== Конец лога ===")
    }

    /**
     * Логирует результат парсинга с информацией о биндингах
     */
    private fun logParsingResultWithBindings(result: SDUIParser.ParsedMicroappResult) {
        logParsingResult(result)

        // Дополнительная информация о биндингах
        Log.d("OpenFileViewModel", "=== Детали биндингов ===")

        result.dataContext?.let { context ->
            Log.d("OpenFileViewModel", "Контекст данных:")
            Log.d("OpenFileViewModel", "  JSON источников: ${context.jsonSources.size}")
            context.jsonSources.forEach { (key, value) ->
                Log.d("OpenFileViewModel", "    $key: ${value.length()} байт")
            }
            Log.d("OpenFileViewModel", "  Query результатов: ${context.queryResults.size}")
            Log.d("OpenFileViewModel", "  ScreenQuery результатов: ${context.screenQueryResults.size}")

            context.screenQueryResults.keys.forEach { key ->
                val value = context.screenQueryResults[key]
                Log.d("OpenFileViewModel", "    $key: ${value?.let {
                    if (it is JSONArray) "JSONArray(${it.length()} элементов)"
                    else it.javaClass.simpleName
                }}")
            }
        } ?: run {
            Log.d("OpenFileViewModel", "Контекст данных не создан")
        }

        val bindingStats = result.countAllBindings()
        val resolvedValues = result.getResolvedValues()

        Log.d("OpenFileViewModel", "Статистика биндингов:")
        Log.d("OpenFileViewModel", "  Всего: $bindingStats")
        Log.d("OpenFileViewModel", "  Разрешено: ${resolvedValues.size}")
        Log.d("OpenFileViewModel", "  Неразрешено: ${bindingStats - resolvedValues.size}")

        if (resolvedValues.isNotEmpty()) {
            Log.d("OpenFileViewModel", "Примеры разрешенных значений:")
            resolvedValues.entries.take(5).forEach { (key, value) ->
                Log.d("OpenFileViewModel", "  $key = $value")
            }
        }

        Log.d("OpenFileViewModel", "=== Конец деталей биндингов ===")
    }

    /**
     * Рекурсивно логирует структуру компонентов
     */
    private fun logComponentStructure(
        component: com.example.drivenui.parser.models.Component,
        indent: String
    ) {
        val typeName = when (component.type) {
            com.example.drivenui.parser.models.ComponentType.SCREEN_LAYOUT -> "ScreenLayout"
            com.example.drivenui.parser.models.ComponentType.LAYOUT -> "Layout"
            com.example.drivenui.parser.models.ComponentType.WIDGET -> "Widget"
            com.example.drivenui.parser.models.ComponentType.SCREEN -> "Screen"
        }

        Log.d("OpenFileViewModel", "$indent$typeName: ${component.title} (${component.code})")
        Log.d("OpenFileViewModel", "$indent  Детей: ${component.children.size}")
        Log.d("OpenFileViewModel", "$indent  Стилей: ${component.styles.size}")
        Log.d("OpenFileViewModel", "$indent  Событий: ${component.events.size}")

        // Логируем свойства с биндингами
        component.properties.forEach { property ->
            if (property.hasBindings) {
                Log.d("OpenFileViewModel", "$indent  Свойство '${property.code}':")
                Log.d("OpenFileViewModel", "$indent    Исходное: ${property.rawValue}")
                Log.d("OpenFileViewModel", "$indent    Разрешенное: ${property.resolvedValue}")
                property.bindings.forEach { binding ->
                    Log.d("OpenFileViewModel", "$indent    Биндинг: ${binding.expression}")
                }
            }
        }

        component.children.forEach { child ->
            logComponentStructure(child, "$indent  ")
        }
    }
}