package com.example.drivenui.presentation.openFile.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.domain.FileInteractor
import com.example.drivenui.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.model.OpenFileState
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.utile.CoreMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class OpenFileViewModel @Inject constructor(
    private val interactor: FileInteractor
) : CoreMviViewModel<OpenFileEvent, OpenFileState, OpenFileEffect>() {

    override fun createInitialState() = OpenFileState()

    override fun handleEvent(event: OpenFileEvent) {
        when (event) {
            OpenFileEvent.OnBackClick -> {
                setEffect { OpenFileEffect.GoBack }
            }

            OpenFileEvent.OnUploadFile -> {
                handleUploadFile()
            }

            OpenFileEvent.OnUploadFileWithBindings -> {
                handleUploadFileWithBindings()
            }

            OpenFileEvent.OnShowFile -> {
                handleShowFile()
            }

            OpenFileEvent.OnShowParsingDetails -> {
                handleShowParsingDetails()
            }

            OpenFileEvent.OnShowBindingStats -> {
                handleShowBindingStats()
            }

            OpenFileEvent.OnLoadJsonFiles -> {
                handleLoadJsonFiles()
            }

            OpenFileEvent.OnParseWithData -> {
                handleParseWithData()
            }

            is OpenFileEvent.OnSelectJsonFiles -> {
                handleSelectJsonFiles(event.files)
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

                // Загружаем и парсим файл с новой структурой (старый метод без биндингов)
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
     * Обрабатывает загрузку файла с биндингами
     */
    private fun handleUploadFileWithBindings() {
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

                // Получаем доступные JSON файлы
                val jsonFiles = interactor.getAvailableJsonFiles()
                Log.d("OpenFileViewModel", "Найдены JSON файлы: ${jsonFiles.joinToString(", ")}")

                // Используем выбранные JSON файлы или берем первые 2
                val selectedJsonFiles = if (uiState.value.selectedJsonFiles.isNotEmpty()) {
                    uiState.value.selectedJsonFiles
                } else {
                    jsonFiles.take(2)
                }

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            selectedFileName = fileName,
                            availableJsonFiles = jsonFiles,
                            selectedJsonFiles = selectedJsonFiles
                        )
                    }
                }

                Log.d("OpenFileViewModel", "Начинаем парсинг файла с биндингами: $fileName")
                Log.d("OpenFileViewModel", "Используем JSON файлы: $selectedJsonFiles")

                // Загружаем и парсим файл с биндингами
                val parsedResult = interactor.parseFileFromAssets(fileName, selectedJsonFiles)

                Log.d("OpenFileViewModel", "Результат парсинга с биндингами: ${parsedResult.screens.size} экранов")

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

                // Логируем результат с биндингами
                logParsingResultWithBindings(parsedResult)

                // Получаем статистику по биндингам
                val bindingStats = interactor.getBindingStats()
                val resolvedValues = interactor.getResolvedValues()

                // Сохраняем статистику в состоянии
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            bindingStats = bindingStats,
                            resolvedValues = resolvedValues
                        )
                    }
                }

                // Создаем сообщение об успехе
                val successMessage = buildString {
                    append("Файл '$fileName' успешно спарсен с биндингами!\n")
                    append("Результат:\n")
                    parsedResult.microapp?.let { append("• Микроапп: ${it.title}\n") }
                    append("• Экран${if (parsedResult.screens.size != 1) "ов" else ""}: ${parsedResult.screens.size}\n")

                    if (bindingStats != null) {
                        append("• Биндинги: ${bindingStats["totalBindings"]} (${bindingStats["resolvedBindings"]} разрешено)\n")
                        append("• JSON файлов: ${selectedJsonFiles.size}\n")
                    }
                }

                withContext(Dispatchers.Main) {
                    setEffect {
                        OpenFileEffect.ShowSuccessWithBindings(
                            message = successMessage,
                            bindingStats = bindingStats,
                            resolvedValues = resolvedValues
                        )
                    }
                }

                // Показываем диалог с результатами
                parsedResult.microapp?.let { microapp ->
                    withContext(Dispatchers.Main) {
                        setEffect {
                            OpenFileEffect.ShowParsingResultDialog(
                                title = microapp.title,
                                screensCount = parsedResult.screens.size,
                                textStylesCount = parsedResult.styles?.textStyles?.size ?: 0,
                                colorStylesCount = parsedResult.styles?.colorStyles?.size ?: 0,
                                queriesCount = parsedResult.queries.size,
                                bindingsCount = parsedResult.countAllBindings(),
                                resolvedBindingsCount = resolvedValues.size,
                                jsonFilesCount = selectedJsonFiles.size
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при парсинге файла с биндингами", e)

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = "Ошибка: ${e.localizedMessage}"
                        )
                    }

                    setEffect {
                        OpenFileEffect.ShowError("Ошибка при парсинге с биндингами: ${e.localizedMessage}")
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
     * Парсинг с кастомными данными
     */
    private fun handleParseWithData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    updateState { copy(isParsing = true, errorMessage = null) }
                }

                val files = interactor.getAvailableFiles()
                val fileName = files.firstOrNull { it.endsWith(".xml") } ?: return@launch

                // Пример кастомных данных
                val jsonData = mapOf(
                    "user" to """{"name": "Иван", "email": "ivan@example.com", "age": 30}""",
                    "settings" to """{"theme": "dark", "notifications": true}"""
                )

                val queryResults = mapOf(
                    "userProfile" to mapOf("id" to 123, "username" to "test_user"),
                    "carriers" to listOf("MTS", "Beeline", "Megafon")
                )

                Log.d("OpenFileViewModel", "Парсинг с кастомными данными: $fileName")

                val result = interactor.parseWithCustomData(
                    fileName = fileName,
                    jsonData = jsonData,
                    queryResults = queryResults
                )

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isParsing = false,
                            parsingResult = result,
                            errorMessage = null
                        )
                    }
                }

                // Получаем разрешенные значения
                val resolvedValues = interactor.getResolvedValues()

                Log.d("OpenFileViewModel", "Разрешено значений: ${resolvedValues.size}")
                resolvedValues.entries.take(3).forEach { (key, value) ->
                    Log.d("OpenFileViewModel", "  $key = $value")
                }

                setEffect {
                    OpenFileEffect.ShowSuccess(
                        "Парсинг с кастомными данными завершен\n" +
                                "Биндингов разрешено: ${resolvedValues.size}"
                    )
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при парсинге с кастомными данными", e)
                updateState { copy(isParsing = false, errorMessage = e.localizedMessage) }
                setEffect { OpenFileEffect.ShowError("Ошибка: ${e.localizedMessage}") }
            }
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

        // Исправление: Проверяем наличие поля bindings в Component
        // Если в вашей модели Component нет поля bindings, нужно использовать другое поле
        // Например, если есть bindingProperties или что-то подобное

        // Вариант 1: Если есть bindingProperties
        if (component.bindingProperties.isNotEmpty()) {
            Log.d("OpenFileViewModel", "$indent  Биндингов: ${component.bindingProperties.size}")
            component.bindingProperties.take(3).forEach { binding ->
                Log.d("OpenFileViewModel", "$indent    - $binding")
            }
        }

        // Вариант 2: Если нужно искать биндинги в свойствах
        val bindingsInProperties = component.properties.flatMap { it.bindings }
        if (bindingsInProperties.isNotEmpty()) {
            Log.d("OpenFileViewModel", "$indent  Биндингов в свойствах: ${bindingsInProperties.size}")
            bindingsInProperties.take(3).forEach { binding ->
                Log.d("OpenFileViewModel", "$indent    - ${binding.expression}")
            }
        }

        component.children.forEach { child ->
            logComponentStructure(child, "$indent  ")
        }
    }
}