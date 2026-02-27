package com.example.drivenui.app.presentation.openFile.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.app.domain.FileDownloadInteractor
import com.example.drivenui.app.domain.FileInteractor
import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.app.domain.toArchiveDownloadFormat
import com.example.drivenui.app.presentation.openFile.model.MicroappItem
import com.example.drivenui.app.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.app.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.app.presentation.openFile.model.OpenFileState
import com.example.drivenui.engine.parser.SDUIParser
import com.example.drivenui.utile.CoreMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class OpenFileViewModel @Inject constructor(
    private val fileInteractor: FileInteractor,
    private val fileDownloadInteractor: FileDownloadInteractor,
    private val microappStorage: MicroappStorage,
    private val microappSource: MicroappSource,
) : CoreMviViewModel<OpenFileEvent, OpenFileState, OpenFileEffect>() {

    init {
        loadJsonFilesOnInit()
        loadSavedMicroapps()
    }

    override fun createInitialState() = OpenFileState(
        microappSource = microappSource,
    )

    override fun handleEvent(event: OpenFileEvent) {
        when (event) {
            OpenFileEvent.OnBackClick -> setEffect { OpenFileEffect.GoBack }
            OpenFileEvent.OnUpload -> {
                handleUpload()
            }
            OpenFileEvent.OnShowFile -> handleShowFile()
            OpenFileEvent.OnShowParsingDetails -> handleShowParsingDetails()
            is OpenFileEvent.OnShowTestScreen -> handleShowTestScreen(event.microappCode)
            OpenFileEvent.OnShowBindingStats -> handleShowBindingStats()
            OpenFileEvent.OnLoadJsonFiles -> handleLoadJsonFiles()
            is OpenFileEvent.OnQrScanned -> handleQrScanned(event.url)
            is OpenFileEvent.OnSelectJsonFiles -> handleSelectJsonFiles(event.files)
        }
    }

    private fun handleUpload() {
        when (microappSource) {
            MicroappSource.ASSETS -> {
                handleUploadFile()
            }

            MicroappSource.FILE_SYSTEM,
            MicroappSource.FILE_SYSTEM_JSON -> {
                setEffect { OpenFileEffect.OpenQrScanner }
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг файла (полный микроапп или шаблон).
     * Логика парсинга единая и допускает отсутствие microapp.xml / allQueries.xml.
     */
    private fun handleUploadFile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = true,
                            isParsing = true,
                            errorMessage = null,
                            selectedFileName = "microapps",
                        )
                    }
                }

                // Единый путь парсинга: full microapp ИЛИ шаблон (экраны + стили)
                val parsedResult = fileInteractor.parseTemplate()

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            parsingResult = parsedResult,
                            errorMessage = null,
                        )
                    }
                    setEffect { OpenFileEffect.ShowSuccess("Конфигурация успешно загружена") }
                    loadSavedMicroapps()
                }

                logParsingResult(parsedResult)

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке или парсинге", e)
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = e.localizedMessage,
                        )
                    }
                    setEffect { OpenFileEffect.ShowError("Ошибка: ${e.localizedMessage}") }
                }
            }
        }
    }

    private fun handleQrScanned(url: String) {
        if (!url.startsWith("http")) {
            setEffect { OpenFileEffect.ShowError("QR не содержит корректную ссылку") }
            return
        }

        val format = microappSource.toArchiveDownloadFormat()
        if (format == null) {
            setEffect { OpenFileEffect.ShowError("Режим загрузки по QR недоступен для текущего источника") }
            return
        }

        viewModelScope.launch {
            try {
                updateState { copy(isUploadFile = true, isParsing = true, errorMessage = null) }

                val success = withContext(Dispatchers.IO) {
                    fileDownloadInteractor.downloadAndExtractZip(url, format)
                }

                if (!success) {
                    setEffect { OpenFileEffect.ShowError("Не удалось загрузить архив") }
                    updateState { copy(isUploadFile = false, isParsing = false) }
                    return@launch
                }
                handleUploadFile()
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка загрузки по QR", e)
                updateState { copy(isUploadFile = false, isParsing = false) }
                val message = when (e) {
                    is java.net.UnknownHostException -> "Нет подключения к сети"
                    is java.net.SocketTimeoutException -> "Превышено время ожидания ответа сервера"
                    is java.io.IOException -> "Ошибка сети: ${e.message ?: e.localizedMessage}"
                    is IllegalArgumentException -> "Некорректные данные: ${e.message ?: e.localizedMessage}"
                    else -> "Ошибка загрузки: ${e.message ?: e.localizedMessage}"
                }
                setEffect { OpenFileEffect.ShowError(message) }
            }
        }
    }

    private fun loadJsonFilesOnInit() {
        viewModelScope.launch {
            try {
                val jsonFiles = fileInteractor.getAvailableJsonFiles()
                updateState { copy(availableJsonFiles = jsonFiles) }

                val selectedFiles = jsonFiles.take(2)
                if (selectedFiles.isNotEmpty()) {
                    updateState { copy(selectedJsonFiles = selectedFiles) }
                    Log.d("OpenFileViewModel", "Автовыбор JSON файлов: ${selectedFiles.joinToString(", ")}")
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON файлов", e)
            }
        }
    }

    private fun handleLoadJsonFiles() {
        viewModelScope.launch {
            try {
                val jsonFiles = fileInteractor.getAvailableJsonFiles()
                updateState { copy(availableJsonFiles = jsonFiles) }

                if (jsonFiles.isEmpty()) setEffect { OpenFileEffect.ShowError("JSON файлы не найдены") }
                else setEffect { OpenFileEffect.ShowSuccess("Найдено ${jsonFiles.size} JSON файлов") }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON", e)
                setEffect { OpenFileEffect.ShowError("Ошибка при загрузке JSON файлов") }
            }
        }
    }

    private fun handleSelectJsonFiles(files: List<String>) {
        updateState { copy(selectedJsonFiles = files) }
        setEffect {
            OpenFileEffect.ShowJsonFileSelectionDialog(
                availableFiles = uiState.value.availableJsonFiles,
                selectedFiles = files,
            )
        }
    }

    // =====================================================
    // Статистика и биндинги
    // =====================================================

    private fun handleShowBindingStats() {
        val resolvedValues = fileInteractor.getResolvedValues()
        val bindingStats = fileInteractor.getBindingStats().orEmpty()

        if (resolvedValues.isEmpty() && bindingStats.isEmpty()) {
            setEffect { OpenFileEffect.ShowError("Сначала загрузите файл с биндингами") }
            return
        }

        setEffect {
            OpenFileEffect.ShowBindingStats(
                stats = bindingStats,
                resolvedValues = resolvedValues,
            )
        }
    }

    private fun handleShowFile() {
        uiState.value.parsingResult?.let {
            setEffect { OpenFileEffect.NavigateToParsingDetails(it) }
        } ?: setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
    }

    private fun handleShowParsingDetails() = handleShowFile()
    private fun loadSavedMicroapps() {
        viewModelScope.launch(Dispatchers.IO) {
            val codes = microappStorage.getAllCodes()
            val items = codes.mapNotNull { code ->
                microappStorage.loadMapped(code)?.let { data ->
                    MicroappItem(
                        code = data.microappCode,
                        title = data.microappTitle.takeIf { it.isNotBlank() } ?: data.microappCode,
                    )
                }
            }
            withContext(Dispatchers.Main) {
                updateState { copy(savedMicroapps = items) }
            }
        }
    }

    private fun handleShowTestScreen(microappCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val mappedData = fileInteractor.loadCachedMicroapp(microappCode)
                ?: microappStorage.loadMapped(microappCode)
            withContext(Dispatchers.Main) {
                if (mappedData != null) {
                    setEffect { OpenFileEffect.NavigateToTestScreen(mappedData) }
                } else {
                    setEffect { OpenFileEffect.ShowError("Микроапп не найден") }
                }
            }
        }
    }

    private fun countComponents(component: com.example.drivenui.engine.parser.models.Component?): Int {
        if (component == null) return 0
        return 1 + component.children.sumOf { countComponents(it) }
    }

    private fun logParsingResult(result: SDUIParser.ParsedMicroappResult) {
        Log.d("OpenFileViewModel", "=== Результат парсинга ===")
        Log.d("OpenFileViewModel", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("OpenFileViewModel", "Экранов: ${result.screens.size}")
        Log.d("OpenFileViewModel", "Запросов API: ${result.queries.size}")

        result.screens.forEachIndexed { index, screen ->
            Log.d("OpenFileViewModel", "Экран ${index + 1}: ${screen.title} (${screen.screenCode})")
            screen.rootComponent?.let { root ->
                Log.d("OpenFileViewModel", "  Компонентов в дереве: ${countComponents(root)}")
                logComponentStructure(root, "    ")
            }
        }

        val resolvedValues = fileInteractor.getResolvedValues()
        Log.d("OpenFileViewModel", "Разрешено биндингов: ${resolvedValues.size}")
        resolvedValues.entries.take(5).forEach { (k, v) ->
            Log.d("OpenFileViewModel", "  $k = $v")
        }
        Log.d("OpenFileViewModel", "=== Конец лога ===")
    }

    private fun logComponentStructure(
        component: com.example.drivenui.engine.parser.models.Component,
        indent: String
    ) {
        Log.d("OpenFileViewModel", "$indent${component.type}: ${component.title} (${component.code})")
        component.children.forEach { logComponentStructure(it, "$indent  ") }
    }
}
