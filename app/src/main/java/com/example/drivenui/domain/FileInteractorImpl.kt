package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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

    override suspend fun parseFileFromAssets(fileName: String): SDUIParser.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг файла из assets: $fileName")

                val result =
                    parserNew.parseFromAssetsNew(fileName).also {
                        Log.d("FileInteractor", "Использован parseFromAssetsNew (без биндингов)")
                    }


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

    override fun getAvailableFiles(): List<String> {
        return try {
            // Получаем список файлов из assets
            context.assets.list("")?.filter {
                it.endsWith(".xml") || it.endsWith(".zip") || it.endsWith(".json")
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e("FileInteractor", "Ошибка при получении списка файлов", e)
            emptyList()
        }
    }

    /**
     * НОВЫЙ МЕТОД: Получить список JSON файлов
     */
    override fun getAvailableJsonFiles(): List<String> {
        return try {
            context.assets.list("")?.filter {
                it.endsWith(".json")
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e("FileInteractor", "Ошибка при получении списка JSON файлов", e)
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

    /**
     * НОВЫЙ МЕТОД: Загрузить JSON файл
     */
    override suspend fun loadJsonFile(fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(fileName)
                inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при загрузке JSON файла $fileName", e)
                throw e
            }
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

        val bindingCount = parsedMicroapp.countAllBindings()
        Log.d("FileInteractor", "Всего биндингов в результате: $bindingCount")
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

            // Проверяем биндинги
            val totalBindings = result.countAllBindings()
            if (totalBindings > 0) {
                val resolvedValues = result.getResolvedValues()
                val resolvedCount = resolvedValues.size
                Log.d("FileInteractor",
                    "Биндинги: всего $totalBindings, разрешено $resolvedCount"
                )

                // Если есть неразрешенные биндинги, предупреждаем
                if (resolvedCount < totalBindings) {
                    Log.w("FileInteractor",
                        "Внимание: ${totalBindings - resolvedCount} биндингов не разрешено"
                    )

                    // Логируем неразрешенные биндинги для отладки
                    Log.d("FileInteractor", "Неразрешенные биндинги:")
                    result.screens.forEach { screen ->
                        screen.rootComponent?.let { root ->
                            findUnresolvedBindings(root).forEach { binding ->
                                Log.d("FileInteractor", "  $binding")
                            }
                        }
                    }
                }
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
                if (property.hasBindings && property.resolvedValue == property.rawValue) {
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
                "bindings" to result.countAllBindings(),
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
            val totalBindings = result.countAllBindings()
            val screenQueryResolved = result.screenQueries.count {
                it.code in (result.dataContext?.screenQueryResults?.keys ?: emptySet())
            }

            mapOf(
                "totalBindings" to totalBindings,
                "resolvedBindings" to resolvedValues.size,
                "unresolvedBindings" to (totalBindings - resolvedValues.size),
                "screenQueryBindings" to screenQueryResolved,
                "resolutionRate" to if (totalBindings > 0)
                    resolvedValues.size.toFloat() / totalBindings else 0f,
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
        Log.d("FileInteractor", "Количество биндингов: ${result.countAllBindings()}")
        Log.d("FileInteractor", "=== Конец лога ===")
    }

    /**
     * Логирует результаты биндингов
     */
    private fun logBindingResults(result: SDUIParser.ParsedMicroappResult) {
        val bindingCount = result.countAllBindings()
        if (bindingCount == 0) {
            Log.d("FileInteractor", "Биндинги не найдены в результате")
            return
        }

        Log.d("FileInteractor", "=== Результаты биндингов ===")
        Log.d("FileInteractor", "Всего биндингов: $bindingCount")

        val resolvedValues = result.getResolvedValues()
        if (resolvedValues.isNotEmpty()) {
            Log.d("FileInteractor", "Разрешенные значения (первые 5):")
            resolvedValues.entries.take(5).forEach { (key, value) ->
                Log.d("FileInteractor", "  $key = $value")
            }
            if (resolvedValues.size > 5) {
                Log.d("FileInteractor", "  ... и еще ${resolvedValues.size - 5} значений")
            }
        } else {
            Log.d("FileInteractor", "Нет разрешенных значений - все биндинги остались макросами")
            Log.d("FileInteractor", "ВНИМАНИЕ: Возможно данные не загружены или контекст данных пуст")
        }

        result.dataContext?.let { context ->
            Log.d("FileInteractor", "Контекст данных:")
            Log.d("FileInteractor", "  JSON источников: ${context.jsonSources.size}")
            context.jsonSources.forEach { (key, value) ->
                Log.d("FileInteractor", "    $key: ${value.length()} элементов")
            }
            Log.d("FileInteractor", "  Query результатов: ${context.queryResults.size}")
            context.queryResults.keys.forEach { key ->
                Log.d("FileInteractor", "    $key: ${context.queryResults[key]?.javaClass?.simpleName}")
            }
            Log.d("FileInteractor", "  ScreenQuery результатов: ${context.screenQueryResults.size}")
            context.screenQueryResults.keys.forEach { key ->
                val value = context.screenQueryResults[key]
                Log.d("FileInteractor", "    $key: ${value?.javaClass?.simpleName}")
                if (value is JSONArray) {
                    Log.d("FileInteractor", "      элементов: ${value.length()}")
                    if (value.length() > 0) {
                        Log.d("FileInteractor", "      пример: ${value.getJSONObject(0).toString().take(100)}...")
                    }
                }
            }
            Log.d("FileInteractor", "  AppState: ${context.appState.size}")
            Log.d("FileInteractor", "  LocalVariables: ${context.localVariables.size}")
        } ?: run {
            Log.d("FileInteractor", "Контекст данных не создан")
        }

        Log.d("FileInteractor", "=== Конец лога биндингов ===")
    }
}