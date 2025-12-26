package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.parser.models.ParsedScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return parseFileFromAssets(fileName, emptyList())
    }

    /**
     * НОВЫЙ МЕТОД: Парсинг с поддержкой JSON данных для биндингов
     */
    override suspend fun parseFileFromAssets(
        fileName: String,
        jsonFileNames: List<String>
    ): SDUIParser.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Начинаем парсинг файла из assets: $fileName")
                Log.d("FileInteractor", "JSON файлы для биндингов: $jsonFileNames")

                // Вариант 1: Если есть JSON файлы - используем parseWithDataBinding
                val result = if (jsonFileNames.isNotEmpty()) {
                    parserNew.parseWithDataBinding(
                        fileName = fileName,
                        jsonFileNames = jsonFileNames
                    ).also {
                        Log.d("FileInteractor", "Использован parseWithDataBinding с биндингами")
                        logBindingResults(it)
                    }
                } else {
                    // Вариант 2: Если нет JSON файлов - обычный парсинг
                    parserNew.parseFromAssetsNew(fileName).also {
                        Log.d("FileInteractor", "Использован parseFromAssetsNew (без биндингов)")
                    }
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

    /**
     * НОВЫЙ МЕТОД: Парсинг специфичного экрана с данными (например, carriers)
     */
    override suspend fun parseCarriersScreenWithData(): ParsedScreen? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Парсинг экрана carriers с данными JSON")

                val carriersScreen = parserNew.parseCarriersScreenWithData()

                if (carriersScreen != null) {
                    Log.d("FileInteractor", "Экран carriers успешно найден и обработан")

                    // Обновляем последний результат
                    val currentResult = lastParsedResult
                    if (currentResult != null) {
                        val updatedScreens = currentResult.screens.map { screen ->
                            if (screen.screenCode == "carriers") carriersScreen else screen
                        }
                        lastParsedResult = currentResult.copy(screens = updatedScreens)
                    }
                } else {
                    Log.w("FileInteractor", "Экран carriers не найден или не удалось обработать")
                }

                carriersScreen
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при парсинге экрана carriers", e)
                null
            }
        }
    }

    /**
     * НОВЫЙ МЕТОД: Парсинг с кастомными данными для биндингов
     */
    override suspend fun parseWithCustomData(
        fileName: String,
        jsonData: Map<String, String>,
        queryResults: Map<String, Any>
    ): SDUIParser.ParsedMicroappResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Парсинг с кастомными данными: $fileName")
                Log.d("FileInteractor", "JSON данных: ${jsonData.size}, Query результатов: ${queryResults.size}")

                // Пока просто используем стандартный метод, можно расширить позже
                val result = parserNew.parseWithDataBinding(
                    fileName = fileName,
                    jsonFileNames = emptyList(), // Здесь нужно преобразовать Map в JSON файлы
                    queryResults = queryResults
                )

                Log.d("FileInteractor", "Парсинг с кастомными данными завершен")
                logBindingResults(result)

                // Сохраняем результат
                lastParsedResult = result

                result
            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка при парсинге с кастомными данными", e)
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

    override fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult) {
        lastParsedResult = parsedMicroapp
        Log.d("FileInteractor", "Результат парсинга сохранен")

        // Логируем информацию о биндингах
        parsedMicroapp.dataContext?.let { context ->
            Log.d("FileInteractor", "Контекст данных:")
            Log.d("FileInteractor", "  JSON источников: ${context.jsonSources.size}")
            Log.d("FileInteractor", "  Query результатов: ${context.queryResults.size}")
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
                "layouts" to result.layouts.size,
                "bindings" to result.countAllBindings(), // ДОБАВЛЕНО: количество биндингов
                "dataContext" to (result.dataContext != null) // ДОБАВЛЕНО: есть ли контекст данных
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

            mapOf(
                "totalBindings" to totalBindings,
                "resolvedBindings" to resolvedValues.size,
                "unresolvedBindings" to (totalBindings - resolvedValues.size),
                "resolutionRate" to if (totalBindings > 0)
                    resolvedValues.size.toFloat() / totalBindings else 0f,
                "resolvedValues" to resolvedValues.entries.take(5).associate { it.key to it.value }, // Первые 5 значений для примера
                "hasDataContext" to (result.dataContext != null)
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
            Log.d("FileInteractor", "ВНИМАНИЕ: Возможно JSON файлы не загружены или контекст данных пуст")
        }

        result.dataContext?.let { context ->
            Log.d("FileInteractor", "Контекст данных:")
            Log.d("FileInteractor", "  JSON источников: ${context.jsonSources.size}")
            context.jsonSources.forEach { (key, value) ->
                Log.d("FileInteractor", "    $key: ${value.length()} элементов")
            }
            Log.d("FileInteractor", "  Query результатов: ${context.queryResults.size}")
        } ?: run {
            Log.d("FileInteractor", "Контекст данных не создан")
        }

        Log.d("FileInteractor", "=== Конец лога биндингов ===")
    }
}