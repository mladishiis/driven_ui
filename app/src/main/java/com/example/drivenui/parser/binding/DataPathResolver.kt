package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.DataContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Разрешитель путей данных с поддержкой вложенных путей и индексации массивов
 */
class DataPathResolver {

    companion object {
        private const val TAG = "DataPathResolver"
        private val BINDING_PATTERN = Regex("""\$\{([^}]+)\}""")
    }

    /**
     * Разрешает путь к данным с поддержкой вложенных путей
     */
    fun resolvePath(path: String, dataContext: DataContext): String? {
        Log.d(TAG, "Начинаем разрешение пути: '$path'")

        // Извлекаем выражение из фигурных скобок
        val matchResult = BINDING_PATTERN.find(path)
        if (matchResult == null) {
            Log.w(TAG, "Неверный формат пути: $path")
            return null
        }

        val fullPath = matchResult.groupValues[1].trim()
        Log.d(TAG, "Полный путь: '$fullPath'")

        // Разбиваем путь на части, учитывая индексацию массивов [index]
        val pathParts = parsePathParts(fullPath)
        Log.d(TAG, "Распарсенные части пути: $pathParts")

        if (pathParts.isEmpty()) {
            Log.w(TAG, "Не удалось распарсить части пути: $fullPath")
            return null
        }

        // Находим корневой источник
        val rootSource = pathParts[0]
        var currentData: Any? = findInDataContext(rootSource.key, dataContext)

        if (currentData == null) {
            Log.w(TAG, "Корневой источник не найден: ${rootSource.key}")
            logAvailableSources(dataContext)
            return null
        }

        Log.d(TAG, "Найден корневой источник: ${rootSource.key} -> ${currentData::class.simpleName}")

        // Применяем индекс к корневому источнику, если он есть
        if (rootSource.index != null) {
            currentData = extractWithIndex(currentData, rootSource.index)
            if (currentData == null) {
                Log.w(TAG, "Не удалось применить индекс ${rootSource.index} к корневому источнику")
                return null
            }
        }

        // Обрабатываем оставшиеся части пути
        for (i in 1 until pathParts.size) {
            val part = pathParts[i]
            currentData = extractData(currentData, part)

            if (currentData == null) {
                Log.w(TAG, "Не удалось извлечь данные на шаге $i: $part")
                return null
            }
        }

        val result = currentData.toString()
        Log.d(TAG, "Успешно разрешено: '$fullPath' -> '$result'")
        return result
    }

    /**
     * Парсит части пути, разделяя на ключи и индексы
     * Пример: "carriers_allCarriers[2].carrierName" ->
     *   [PathPart("carriers_allCarriers", 2), PathPart("carrierName", null)]
     */
    private fun parsePathParts(fullPath: String): List<PathPart> {
        val parts = mutableListOf<PathPart>()
        val regex = Regex("""([^\[\].]+)(?:\[(\d+)\])?""")
        val matches = regex.findAll(fullPath)

        for (match in matches) {
            val key = match.groups[1]?.value ?: continue
            val indexStr = match.groups[2]?.value
            val index = indexStr?.toIntOrNull()
            parts.add(PathPart(key, index))
        }

        return parts
    }

    /**
     * Извлекает данные из текущего источника по части пути
     */
    private fun extractData(current: Any?, part: PathPart): Any? {
        if (current == null) return null

        // Сначала применяем индекс, если он есть
        var result: Any? = current
        if (part.index != null) {
            result = extractWithIndex(result, part.index)
        }

        // Если есть ключ (не только индекс), извлекаем по ключу
        if (part.key.isNotEmpty() && result != null) {
            result = when (result) {
                is JSONObject -> result.opt(part.key)
                is JSONArray -> {
                    // Пытаемся найти в массиве объектов
                    extractFromArrayByKey(result, part.key)
                }
                is Map<*, *> -> (result as Map<String, Any?>)[part.key]
                else -> null
            }
        }

        return result
    }

    /**
     * Извлекает элемент по индексу из массива
     */
    private fun extractWithIndex(data: Any?, index: Int): Any? {
        return when (data) {
            is JSONArray -> {
                if (index >= 0 && index < data.length()) {
                    data.get(index)
                } else {
                    null
                }
            }
            is List<*> -> {
                if (index >= 0 && index < data.size) {
                    data[index]
                } else {
                    null
                }
            }
            is Array<*> -> {
                if (index >= 0 && index < data.size) {
                    data[index]
                } else {
                    null
                }
            }
            else -> {
                Log.w(TAG, "Нельзя применить индекс [$index] к типу: ${data?.javaClass?.simpleName}")
                null
            }
        }
    }

    /**
     * Извлекает данные из массива по ключу
     */
    private fun extractFromArrayByKey(array: JSONArray, key: String): Any? {
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i)
            if (item != null && item.has(key)) {
                return item.get(key)
            }
        }
        return null
    }

    /**
     * Ищет источник в контексте данных
     */
    private fun findInDataContext(sourceKey: String, dataContext: DataContext): Any? {
        // Проверяем в JSON источниках
        dataContext.jsonSources.forEach { (key, value) ->
            // Ищем точное совпадение или часть
            if (key == sourceKey || key.contains(".$sourceKey")) {
                return value
            }
        }

        // Проверяем другие источники
        dataContext.queryResults[sourceKey]?.let { return it }
        dataContext.screenQueryResults[sourceKey]?.let { return it }
        dataContext.appState[sourceKey]?.let { return it }
        dataContext.localVariables[sourceKey]?.let { return it }

        return null
    }

    /**
     * Логирует доступные источники для отладки
     */
    private fun logAvailableSources(dataContext: DataContext) {
        Log.d(TAG, "Доступные источники:")

        Log.d(TAG, "  jsonSources:")
        dataContext.jsonSources.forEach { (key, value) ->
            Log.d(TAG, "    $key: ${if (value is JSONArray) "JSONArray(${value.length()})" else value}")
        }

        Log.d(TAG, "  screenQueryResults: ${dataContext.screenQueryResults.keys}")
        Log.d(TAG, "  queryResults: ${dataContext.queryResults.keys}")
        Log.d(TAG, "  appState: ${dataContext.appState.keys}")
        Log.d(TAG, "  localVariables: ${dataContext.localVariables.keys}")
    }

    /**
     * Класс для представления части пути (ключ + индекс)
     */
    private data class PathPart(
        val key: String,
        val index: Int?
    )
}