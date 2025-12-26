package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.DataContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Разрешитель путей данных с поддержкой вложенных путей
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

        // Разбиваем путь на части по точкам
        val pathParts = fullPath.split(".")
        Log.d(TAG, "Части пути: $pathParts")

        // Начинаем поиск с первого источника
        var currentSource: Any? = null
        var currentKey: String = pathParts[0]
        var currentPath = mutableListOf<String>()

        // Пытаемся найти источник в различных контекстах
        currentSource = findInDataContext(currentKey, dataContext)

        if (currentSource == null) {
            Log.w(TAG, "Источник не найден: $currentKey")
            logAvailableSources(dataContext)
            return null
        }

        Log.d(TAG, "Найден источник: $currentKey")

        // Если есть только источник без дальнейшего пути
        if (pathParts.size == 1) {
            return currentSource.toString()
        }

        // Обрабатываем оставшиеся части пути
        currentPath.addAll(pathParts.subList(1, pathParts.size))

        // Извлекаем данные по пути
        val result = extractDataFromSource(currentSource, currentPath)
        if (result != null) {
            Log.d(TAG, "Найдено значение: $result")
        } else {
            Log.w(TAG, "Не удалось извлечь данные по пути: $currentPath")
        }

        return result?.toString()
    }

    /**
     * Ищет источник в контексте данных
     */
    private fun findInDataContext(sourceKey: String, dataContext: DataContext): Any? {
        // Проверяем в JSON источниках (ищем полное совпадение или часть)
        dataContext.jsonSources.forEach { (key, value) ->
            if (key == sourceKey) {
                return value
            }
            // Проверяем, является ли ключ частью полного ключа
            if (key.startsWith("$sourceKey.")) {
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
     * Извлекает данные из источника по пути
     */
    private fun extractDataFromSource(source: Any, path: List<String>): Any? {
        var current: Any? = source

        for (part in path) {
            if (current == null) return null

            current = when (current) {
                is JSONObject -> {
                    // Проверяем, является ли часть индексом массива [0]
                    if (part.matches(Regex("\\[\\d+\\]"))) {
                        // current должен быть JSONArray, но это JSONObject
                        return null
                    }
                    current.opt(part)
                }
                is JSONArray -> {
                    // Проверяем, является ли часть индексом массива
                    if (part.matches(Regex("\\[\\d+\\]"))) {
                        val index = part.removeSurrounding("[", "]").toIntOrNull()
                        if (index != null && index < current.length()) {
                            current.get(index)
                        } else {
                            return null
                        }
                    } else {
                        // Пытаемся получить объект из массива по ключу
                        // (например, если массив содержит объекты)
                        return extractFromArrayByKey(current, part)
                    }
                }
                is Map<*, *> -> (current as Map<String, Any>)[part]
                else -> return null
            }
        }

        return current
    }

    /**
     * Извлекает данные из массива по ключу
     */
    private fun extractFromArrayByKey(array: JSONArray, key: String): Any? {
        // Если массив содержит объекты, ищем первый объект с указанным ключом
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i)
            if (item != null && item.has(key)) {
                return item.get(key)
            }
        }
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
}