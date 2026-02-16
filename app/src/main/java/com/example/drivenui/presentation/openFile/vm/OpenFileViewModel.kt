package com.example.drivenui.presentation.openFile.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.domain.FileDownloadInteractor
import com.example.drivenui.domain.FileInteractor
import com.example.drivenui.domain.MicroappSource
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.model.OpenFileState
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
    private val microappSource: MicroappSource
) : CoreMviViewModel<OpenFileEvent, OpenFileState, OpenFileEffect>() {

    init {
        loadJsonFilesOnInit()
    }

    override fun createInitialState() = OpenFileState(
        microappSource = microappSource
    )

    override fun handleEvent(event: OpenFileEvent) {
        when (event) {
            OpenFileEvent.OnBackClick -> setEffect { OpenFileEffect.GoBack }
            OpenFileEvent.OnUpload -> {
                updateState { copy(pendingLoadAsTemplate = false) }
                handleUpload()
            }
            OpenFileEvent.OnLoadTemplate -> {
                updateState { copy(pendingLoadAsTemplate = true) }
                when (microappSource) {
                    MicroappSource.ASSETS -> handleLoadTemplate()
                    MicroappSource.FILE_SYSTEM -> setEffect { OpenFileEffect.OpenQrScanner }
                }
            }
            OpenFileEvent.OnShowFile -> handleShowFile()
            OpenFileEvent.OnShowParsingDetails -> handleShowParsingDetails()
            OpenFileEvent.OnShowTestScreen -> handleShowTestScreen()
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

            MicroappSource.FILE_SYSTEM -> {
                setEffect { OpenFileEffect.OpenQrScanner }
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг шаблона (экран + allStyles; microapp и queries опциональны).
     */
    private fun handleLoadTemplate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = true,
                            isParsing = true,
                            errorMessage = null,
                            selectedFileName = "шаблон"
                        )
                    }
                }

                val parsedResult = fileInteractor.parseTemplate()

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            parsingResult = parsedResult,
                            errorMessage = null,
                            pendingLoadAsTemplate = false
                        )
                    }
                    setEffect { OpenFileEffect.ShowSuccess("Шаблон успешно загружен") }
                }

                logParsingResult(parsedResult)

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке шаблона", e)
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = e.localizedMessage,
                            pendingLoadAsTemplate = false
                        )
                    }
                    setEffect { OpenFileEffect.ShowError("Ошибка шаблона: ${e.localizedMessage}") }
                }
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг файла
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
                            selectedFileName = "microapp.xml"
                        )
                    }
                }

                val parsedResult = fileInteractor.parseMicroapp()

                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            parsingResult = parsedResult,
                            errorMessage = null
                        )
                    }
                    setEffect { OpenFileEffect.ShowSuccess("Микроапп успешно загружен") }
                }

                logParsingResult(parsedResult)

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке или парсинге", e)
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = e.localizedMessage
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

        viewModelScope.launch {
            try {
                updateState { copy(isUploadFile = true, isParsing = true, errorMessage = null) }

                val success = withContext(Dispatchers.IO) {
                    fileDownloadInteractor.downloadAndExtractZip(url)
                }

                if (!success) {
                    setEffect { OpenFileEffect.ShowError("Не удалось загрузить архив") }
                    updateState { copy(isUploadFile = false, isParsing = false) }
                    return@launch
                }

                val loadAsTemplate = uiState.value.pendingLoadAsTemplate
                updateState { copy(pendingLoadAsTemplate = false) }
                if (loadAsTemplate) {
                    handleLoadTemplate()
                } else {
                    handleUploadFile()
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка загрузки по QR", e)
                updateState { copy(isUploadFile = false, isParsing = false, pendingLoadAsTemplate = false) }
                setEffect { OpenFileEffect.ShowError("Ошибка загрузки: ${e.localizedMessage}") }
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
                selectedFiles = files
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
                resolvedValues = resolvedValues
            )
        }
    }

    private fun handleShowFile() {
        uiState.value.parsingResult?.let {
            setEffect { OpenFileEffect.NavigateToParsingDetails(it) }
        } ?: setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
    }

    private fun handleShowParsingDetails() = handleShowFile()
    private fun handleShowTestScreen() {
        uiState.value.parsingResult?.let {
            setEffect { OpenFileEffect.NavigateToTestScreen(it) }
        } ?: setEffect { OpenFileEffect.ShowError("Сначала загрузите файл") }
    }

    private fun countComponents(component: com.example.drivenui.parser.models.Component?): Int {
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
        component: com.example.drivenui.parser.models.Component,
        indent: String
    ) {
        Log.d("OpenFileViewModel", "$indent${component.type}: ${component.title} (${component.code})")
        component.children.forEach { logComponentStructure(it, "$indent  ") }
    }
}
