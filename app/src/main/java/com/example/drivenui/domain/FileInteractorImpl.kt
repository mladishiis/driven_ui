package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Реализация FileInteractor с интеграцией парсера SDUI и поддержкой биндингов
 */
internal class FileInteractorImpl @Inject constructor(
    private val fileRepository: FileRepository,
    private val context: Context
) : FileInteractor {

    private var lastParsedResult: SDUIParser.ParsedMicroappResult? = null
    private val parserNew = SDUIParser(context)

    override suspend fun parseMicroappFromAssetsRoot(): SDUIParser.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг файла из assets")
                val extractedDir = File(context.filesDir, "assets_simulation/microappTavrida")
                val result =
                    parserNew.parseFromDir(extractedDir).also {
                        Log.d("FileInteractor", "Использован parseFromAssetsNew")
                    }

                // Сохраняем результат
                lastParsedResult = result

                result
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при парсинге файла", e)
                throw e
            }
        }
    }

    override fun getAvailableFiles(): List<String> {
        val dir = File(context.filesDir, "assets_simulation/microappTavrida")
        return dir.list()?.toList() ?: emptyList()
    }

    /**
     * НОВЫЙ МЕТОД: Получить список JSON файлов
     */
    override fun getAvailableJsonFiles(): List<String> {
        val dir = File(context.filesDir, "assets_simulation/microappTavrida")
        return dir.list()?.filter { it.endsWith(".json") } ?: emptyList()
    }

    override suspend fun loadXmlFile(fileName: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "assets_simulation/microappTavrida/$fileName")
            if (!file.exists()) throw FileNotFoundException("File not found: $fileName")
            file.readText()
        }
    }

    override suspend fun loadJsonFile(fileName: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "assets_simulation/microappTavrida/$fileName")
            if (!file.exists()) throw FileNotFoundException("File not found: $fileName")
            file.readText()
        }
    }

    /**
     * НОВЫЙ МЕТОД: Загрузить JSON файл как JSONArray
     */
    override suspend fun loadJsonFileAsArray(fileName: String): JSONArray? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = loadJsonFile(fileName)
                JSONArray(jsonString)
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при загрузке JSON файла как массива: $fileName", e)
                null
            }
        }
    }

    override fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult) {
        lastParsedResult = parsedMicroapp
        Log.d("FileInteractor", "Результат парсинга сохранен")

        // Логируем информацию о биндингах
        parsedMicroapp.dataContext?.let { context ->
            Log.d("FileInteractor", "Контекст данных:")
            Log.d("FileInteractor", "  JSON источников: ${context.jsonSources.size}")
            Log.d("FileInteractor", "  Query результатов: ${context.queryResults.size}")
            Log.d("FileInteractor", "  ScreenQuery результатов: ${context.screenQueryResults.size}")
            Log.d("FileInteractor", "  AppState: ${context.appState.size}")
            Log.d("FileInteractor", "  LocalVariables: ${context.localVariables.size}")
        }
    }

    override fun getLastParsedResult(): SDUIParser.ParsedMicroappResult? {
        return lastParsedResult
    }

    /**
     * НОВЫЙ МЕТОД: Получить разрешенные значения биндингов
     */
    override fun getResolvedValues(): Map<String, String> {
        return lastParsedResult?.getResolvedValues() ?: emptyMap()
    }

    override suspend fun validateParsingResult(result: SDUIParser.ParsedMicroappResult): Boolean {
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

    /**
     * Вспомогательная функция для поиска неразрешенных биндингов
     */
    private fun findUnresolvedBindings(component: com.example.drivenui.parser.models.Component): List<String> {
        val unresolved = mutableListOf<String>()

        fun searchRecursive(comp: com.example.drivenui.parser.models.Component, path: String) {
            comp.properties.forEach { property ->
                if (property.resolvedValue == property.rawValue) {
                    unresolved.add("$path.${property.code}: ${property.rawValue}")
                }
            }

            comp.children.forEachIndexed { index, child ->
                val childPath = if (path.isEmpty()) "child_$index" else "$path.child_$index"
                searchRecursive(child, childPath)
            }
        }

        searchRecursive(component, component.code)
        return unresolved
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
                "screenQueries" to result.screenQueries.size,
                "widgets" to result.widgets.size,
                "layouts" to result.layouts.size,
                "dataContext" to (result.dataContext != null),
                "screenQueryBindings" to result.screenQueries.count {
                    it.code in (result.dataContext?.screenQueryResults?.keys ?: emptySet())
                }
            )
        }
    }

    /**
     * НОВЫЙ МЕТОД: Получить детальную статистику по биндингам
     */
    override fun getBindingStats(): Map<String, Any>? {
        return lastParsedResult?.let { result ->
            val resolvedValues = result.getResolvedValues()
            val screenQueryResolved = result.screenQueries.count {
                it.code in (result.dataContext?.screenQueryResults?.keys ?: emptySet())
            }

            mapOf(
                "resolvedBindings" to resolvedValues.size,
                "screenQueryBindings" to screenQueryResolved,
                "resolvedValues" to resolvedValues.entries.take(5).associate { it.key to it.value },
                "hasDataContext" to (result.dataContext != null),
                "hasScreenQueryData" to (!result.dataContext?.screenQueryResults.isNullOrEmpty())
            )
        }
    }

    override fun clearParsedData() {
        lastParsedResult = null
        Log.d("FileInteractor", "Данные парсинга очищены")
    }

    /**
     * Логирует результат парсинга с новой структурой
     */
    private fun logParsingResultNew(result: SDUIParser.ParsedMicroappResult, source: String) {
        Log.d("FileInteractor", "=== Результат парсинга ($source) ===")
        Log.d("FileInteractor", "Микроапп: ${result.microapp?.title ?: "Не найден"}")
        Log.d("FileInteractor", "Количество экранов: ${result.screens.size}")
        result.screens.forEachIndexed { index, screen ->
            Log.d("FileInteractor", "  Экран $index: ${screen.title} (${screen.screenCode})")
            if (screen.rootComponent != null) {
                val componentCount = result.countComponentsRecursive(screen.rootComponent)
                Log.d("FileInteractor", "    Есть корневой компонент, всего компонентов: $componentCount")
            }
        }
        Log.d("FileInteractor", "Количество стилей текста: ${result.styles?.textStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество стилей цвета: ${result.styles?.colorStyles?.size ?: 0}")
        Log.d("FileInteractor", "Количество запросов: ${result.queries.size}")
        Log.d("FileInteractor", "Количество экранных запросов: ${result.screenQueries.size}")
        Log.d("FileInteractor", "=== Конец лога ===")
    }
}