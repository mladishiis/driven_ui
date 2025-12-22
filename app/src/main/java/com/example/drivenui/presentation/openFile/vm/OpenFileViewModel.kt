package com.example.drivenui.presentation.openFile.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.domain.FileInteractor
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

            OpenFileEvent.OnShowFile -> {
                handleShowFile()
            }

            OpenFileEvent.OnShowParsingDetails -> {
                handleShowParsingDetails()
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг файла
     */
    private fun handleUploadFile() {
        viewModelScope.launch(Dispatchers.IO) {  // Явно указываем Dispatchers.IO
            try {
                // Устанавливаем состояние загрузки (в основном потоке)
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

                // Загружаем и парсим файл (выполняется в IO потоке)
                val parsedResult = interactor.parseFileFromAssets(fileName)

                // Сохраняем результат (в основном потоке)
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

                // Показываем успешное сообщение (в основном потоке)
                val successMessage = buildString {
                    append("Файл '$fileName' успешно спарсен!\n")
                    append("Результат:\n")
                    parsedResult.microapp?.let { append("• Микроапп: ${it.title}\n") }
                    append("• Экран${if (parsedResult.screens.size != 1) "ов" else ""}: ${parsedResult.screens.size}\n")
                    append("• Стили текста: ${parsedResult.styles?.textStyles?.size ?: 0}\n")
                    append("• Стили цвета: ${parsedResult.styles?.colorStyles?.size ?: 0}\n")
                    append("• Запросов API: ${parsedResult.queries.size}")
                }

                withContext(Dispatchers.Main) {
                    setEffect { OpenFileEffect.ShowSuccess(successMessage) }
                }

                // Показываем диалог с результатами (в основном потоке)
                parsedResult.microapp?.let { microapp ->
                    withContext(Dispatchers.Main) {
                        setEffect {
                            OpenFileEffect.ShowParsingResultDialog(
                                title = microapp.title,
                                screensCount = parsedResult.screens.size,
                                textStylesCount = parsedResult.styles?.textStyles?.size ?: 0,
                                colorStylesCount = parsedResult.styles?.colorStyles?.size ?: 0,
                                queriesCount = parsedResult.queries.size
                            )
                        }
                    }
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
        }
    }

    /**
     * Логирует результат парсинга
     */
    private fun logParsingResult(result: com.example.drivenui.parser.SDUIParser.ParsedMicroapp) {
        Log.d("OpenFileViewModel", "=== Результат парсинга ===")
        Log.d("OpenFileViewModel", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("OpenFileViewModel", "Код: ${result.microapp?.code ?: "Не указан"}")
        Log.d("OpenFileViewModel", "Deeplink: ${result.microapp?.deeplink ?: "Не указан"}")
        Log.d("OpenFileViewModel", "Экранов: ${result.screens.size}")

        result.screens.forEachIndexed { index, screen ->
            Log.d("OpenFileViewModel", "  Экран ${index + 1}: ${screen.title}")
            Log.d("OpenFileViewModel", "    Код: ${screen.screenCode}")
            Log.d("OpenFileViewModel", "    Deeplink: ${screen.deeplink}")
            Log.d("OpenFileViewModel", "    Лэйаутов: ${screen.screenLayouts.size}")
        }

        Log.d("OpenFileViewModel", "Стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("OpenFileViewModel", "Стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("OpenFileViewModel", "Запросов API: ${result.queries.size}")
        Log.d("OpenFileViewModel", "Экранных запросов: ${result.screenQueries.size}")
        Log.d("OpenFileViewModel", "=== Конец лога ===")
    }
}