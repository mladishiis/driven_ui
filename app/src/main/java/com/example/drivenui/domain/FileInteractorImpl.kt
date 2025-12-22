package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Реализация FileInteractor с интеграцией парсера SDUI
 */
internal class FileInteractorImpl @Inject constructor(
    private val fileRepository: FileRepository,
    private val context: Context
) : FileInteractor {

    private var lastParsedResult: SDUIParser.ParsedMicroapp? = null
    private val parser = SDUIParser(context)

    override suspend fun parseFileFromAssets(fileName: String): SDUIParser.ParsedMicroapp {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг файла из assets: $fileName")

                // Загружаем и парсим файл
                val result = parser.parseFromAssets(fileName)

                // Проверяем результат парсинга
                if (!result.hasData()) {
                    Log.w("FileInteractor", "Парсинг завершен, но данные не найдены в файле $fileName")
                } else {
                    Log.d("FileInteractor", "Парсинг успешен, найдены данные")
                    result.logSummary()
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

    override suspend fun parseFileFromZip(filePath: String): SDUIParser.ParsedMicroapp {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг ZIP файла: $filePath")

                // Парсим ZIP архив
                val result = parser.parseFromZip(filePath)

                // Сохраняем результат
                lastParsedResult = result

                // Логируем результат
                logParsingResult(result, "ZIP: $filePath")

                result
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

    override fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroapp) {
        lastParsedResult = parsedMicroapp
        Log.d("FileInteractor", "Результат парсинга сохранен")
    }

    override fun getLastParsedResult(): SDUIParser.ParsedMicroapp? {
        return lastParsedResult
    }

    override suspend fun validateParsingResult(result: SDUIParser.ParsedMicroapp): Boolean {
        return withContext(Dispatchers.IO) {
            // Базовая валидация
            if (!result.hasData()) {
                Log.w("FileInteractor", "Результат парсинга пуст")
                return@withContext false
            }

            // Дополнительные проверки
            val hasValidData = when {
                result.hasMicroapp() -> {
                    val microapp = result.microapp!!
                    microapp.title.isNotEmpty() && microapp.code.isNotEmpty()
                }
                result.hasScreens() -> {
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
                "textStyles" to result.getTextStyles().size,
                "colorStyles" to result.getColorStyles().size,
                "events" to (result.events?.events?.size ?: 0),
                "queries" to result.queries.size,
                "widgets" to result.widgets.size,
                "layouts" to result.layouts.size,
                "hasData" to result.hasData()
            )
        }
    }

    override fun clearParsedData() {
        lastParsedResult = null
        Log.d("FileInteractor", "Данные парсинга очищены")
    }

    /**
     * Логирует результат парсинга
     */
    private fun logParsingResult(result: SDUIParser.ParsedMicroapp, source: String) {
        Log.d("FileInteractor", "=== Результат парсинга ($source) ===")
        Log.d("FileInteractor", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("FileInteractor", "Количество экранов: ${result.screens.size}")
        Log.d("FileInteractor", "Количество стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество запросов: ${result.queries.size}")
        Log.d("FileInteractor", "Количество экранных запросов: ${result.screenQueries.size}")
        Log.d("FileInteractor", "=== Конец лога ===")
    }
}