package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.parser.SDUIParserNew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Реализация FileInteractor с интеграцией парсера SDUI
 */
internal class FileInteractorImpl @Inject constructor(
    private val fileRepository: FileRepository,
    private val context: Context
) : FileInteractor {

    private var lastParsedResult: SDUIParserNew.ParsedMicroappResult? = null
    private val parserOld = SDUIParser(context)
    private val parserNew = SDUIParserNew(context)

    override suspend fun parseFileFromAssets(fileName: String): SDUIParserNew.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг файла из assets: $fileName")

                // Загружаем и парсим файл с новой структурой
                val result = parserNew.parseFromAssetsNew(fileName)

                // Проверяем результат парсинга
                if (result.screens.isEmpty() && result.microapp == null) {
                    Log.w("FileInteractor", "Парсинг завершен, но данные не найдены в файле $fileName")
                } else {
                    Log.d("FileInteractor", "Парсинг успешен, найдены данные")
                    logParsingResultNew(result, "assets: $fileName")
                }

                // Сохраняем результат
                lastParsedResult = result

                result
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при парсинге файла $fileName", e)
                throw e
            }
        }
    }

    override suspend fun parseFileFromZip(filePath: String): SDUIParserNew.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг ZIP файла: $filePath")

                // Используем старый парсер для ZIP, но конвертируем в новую структуру
                val oldResult = parserOld.parseFromZip(filePath)

                // Конвертируем старый результат в новый формат
                val newResult = convertOldToNewResult(oldResult)

                // Парсим также с новым парсером, если нужно
                // val newResultDirect = parserNew.parseFromZip(filePath) // если будет реализовано

                // Сохраняем результат
                lastParsedResult = newResult

                // Логируем результат
                logParsingResultNew(newResult, "ZIP: $filePath")

                newResult
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при парсинге ZIP файла $filePath", e)
                throw e
            }
        }
    }

    override fun getAvailableFiles(): List<String> {
        return try {
            // Получаем список файлов из assets
            context.assets.list("")?.filter {
                it.endsWith(".xml") || it.endsWith(".zip")
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e("FileInteractor", "Ошибка при получении списка файлов", e)
            emptyList()
        }
    }

    override suspend fun loadXmlFile(fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(fileName)
                inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при загрузке файла $fileName", e)
                throw e
            }
        }
    }

    override fun saveParsedResult(parsedMicroapp: SDUIParserNew.ParsedMicroappResult) {
        lastParsedResult = parsedMicroapp
        Log.d("FileInteractor", "Результат парсинга сохранен")
    }

    override fun getLastParsedResult(): SDUIParserNew.ParsedMicroappResult? {
        return lastParsedResult
    }

    override suspend fun validateParsingResult(result: SDUIParserNew.ParsedMicroappResult): Boolean {
        return withContext(Dispatchers.IO) {
            // Базовая валидация
            if (result.screens.isEmpty() && result.microapp == null) {
                Log.w("FileInteractor", "Результат парсинга пуст")
                return@withContext false
            }

            // Дополнительные проверки
            val hasValidData = when {
                result.microapp != null -> {
                    val microapp = result.microapp!!
                    microapp.title.isNotEmpty() && microapp.code.isNotEmpty()
                }
                result.screens.isNotEmpty() -> {
                    result.screens.all { it.screenCode.isNotEmpty() }
                }
                else -> true
            }

            hasValidData
        }
    }

    override fun getParsingStats(): Map<String, Any>? {
        return lastParsedResult?.let { result ->
            mapOf(
                "microapp" to (result.microapp?.title ?: "не найден"),
                "screens" to result.screens.size,
                "textStyles" to (result.styles?.textStyles?.size ?: 0),
                "colorStyles" to (result.styles?.colorStyles?.size ?: 0),
                "events" to (result.events?.events?.size ?: 0),
                "queries" to result.queries.size,
                "widgets" to result.widgets.size,
                "layouts" to result.layouts.size
            )
        }
    }

    override fun clearParsedData() {
        lastParsedResult = null
        Log.d("FileInteractor", "Данные парсинга очищены")
    }

    /**
     * Конвертирует старый результат парсинга в новый формат
     */
    private fun convertOldToNewResult(oldResult: SDUIParser.ParsedMicroapp): SDUIParserNew.ParsedMicroappResult {
        // Пока возвращаем пустой результат, так как конвертация сложная
        // В реальном приложении нужно будет конвертировать структуры
        return SDUIParserNew.ParsedMicroappResult(
            microapp = oldResult.microapp,
            styles = oldResult.styles,
            events = oldResult.events,
            eventActions = oldResult.eventActions,
            screens = emptyList(), // Требуется сложная конвертация
            queries = oldResult.queries,
            screenQueries = oldResult.screenQueries,
            widgets = oldResult.widgets,
            layouts = oldResult.layouts
        )
    }

    /**
     * Логирует результат парсинга с новой структурой
     */
    private fun logParsingResultNew(result: SDUIParserNew.ParsedMicroappResult, source: String) {
        Log.d("FileInteractor", "=== Результат парсинга ($source) ===")
        Log.d("FileInteractor", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("FileInteractor", "Количество экранов: ${result.screens.size}")
        result.screens.forEachIndexed { index, screen ->
            Log.d("FileInteractor", "  Экран $index: ${screen.title} (${screen.screenCode})")
            if (screen.rootComponent != null) {
                Log.d("FileInteractor", "    Есть корневой компонент")
            }
        }
        Log.d("FileInteractor", "Количество стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество запросов: ${result.queries.size}")
        Log.d("FileInteractor", "Количество экранных запросов: ${result.screenQueries.size}")
        Log.d("FileInteractor", "=== Конец лога ===")
    }
}