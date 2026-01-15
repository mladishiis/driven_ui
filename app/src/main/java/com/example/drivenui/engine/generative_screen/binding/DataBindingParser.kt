package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.parser.models.DataBinding
import com.example.drivenui.parser.models.BindingSourceType
import com.example.drivenui.parser.models.DataContext
import org.json.JSONArray
import org.json.JSONObject

object DataBindingParser {


    private const val TAG = "DataBindingParser"


    /**
     * Парсит строку с макросами на список биндингов
     */
    fun parseBindings(text: String): List<DataBinding> {
        val pattern = "\\\$\\{([^}]+)\\}".toRegex()
        val bindings = mutableListOf<DataBinding>()

        Log.d(TAG, "Parsing bindings from: '$text'")

        pattern.findAll(text).forEach { matchResult ->
            val expression = matchResult.value
            val content = matchResult.groupValues[1]

            Log.d(TAG, "Found expression: '$expression', content: '$content'")

            parseBinding(content)?.let { binding ->
                val fullBinding = binding.copy(expression = expression)
                Log.d(TAG, "Parsed binding: $fullBinding")
                bindings.add(fullBinding)
            }
        }

        return bindings
    }

    /**
     * Парсит одиночное выражение биндинга
     */
    private fun parseBinding(content: String): DataBinding? {
        Log.d(TAG, "Parsing binding content: '$content'")

        // Пример: carriers_list[0].carrierName
        // Мы ожидаем формат: sourceName[index].property

        val pattern = "([a-zA-Z_]+)\\[(\\d+)\\](?:\\.(.+))?".toRegex()
        val match = pattern.matchEntire(content)

        return if (match != null) {
            val sourceName = match.groupValues[1] // carriers_list
            val index = match.groupValues[2] // 0
            val property = match.groupValues[3] // carrierName

            Log.d(TAG, "Parsed: source=$sourceName, index=$index, property=$property")

            DataBinding(
                sourceType = BindingSourceType.JSON_FILE,
                sourceName = sourceName,
                path = "[$index].$property", // Формируем путь
                expression = "",
                defaultValue = ""
            )
        } else {
            Log.d(TAG, "Failed to parse binding: '$content'")
            null
        }
    }

    private fun parseArrayAccess(content: String): DataBinding {
        // Пример: carriers_list[0].carrierName
        val pattern = "([^\\[]+)\\[(\\d+)\\](?:\\.(.+))?".toRegex()
        val match = pattern.matchEntire(content)

        return if (match != null) {
            val sourceName = match.groupValues[1] // carriers_list
            val index = match.groupValues[2] // 0
            val path = match.groupValues[3] // carrierName

            // Определяем тип источника
            val sourceType = when {
                sourceName.endsWith("_list") -> BindingSourceType.JSON_FILE
                sourceName.contains(":") -> {
                    val type = sourceName.split(":")[0]
                    when (type) {
                        "json" -> BindingSourceType.JSON_FILE
                        "query" -> BindingSourceType.QUERY_RESULT
                        "screenQuery" -> BindingSourceType.SCREEN_QUERY_RESULT
                        "app" -> BindingSourceType.APP_STATE
                        "local" -> BindingSourceType.LOCAL_VAR
                        else -> BindingSourceType.JSON_FILE
                    }
                }
                else -> BindingSourceType.JSON_FILE
            }

            // Формируем полный путь
            val fullPath = "[$index]${if (path.isNotEmpty()) ".$path" else ""}"

            DataBinding(
                sourceType = sourceType,
                sourceName = sourceName,
                path = fullPath,
                expression = "",
                defaultValue = ""
            )
        } else {
            // Если не удалось распарсить как массив, пробуем обычную нотацию
            parseDotNotation(content)
        }
    }

    private fun parseDotNotation(content: String): DataBinding {
        // Простая точечная нотация без индексов
        val parts = content.split(".")
        val sourceName = parts[0]
        val path = if (parts.size > 1) parts.drop(1).joinToString(".") else ""

        val sourceType = when {
            sourceName.endsWith("_list") -> BindingSourceType.JSON_FILE
            sourceName.contains(":") -> {
                val type = sourceName.split(":")[0]
                when (type) {
                    "json" -> BindingSourceType.JSON_FILE
                    "query" -> BindingSourceType.QUERY_RESULT
                    "screenQuery" -> BindingSourceType.SCREEN_QUERY_RESULT
                    "app" -> BindingSourceType.APP_STATE
                    "local" -> BindingSourceType.LOCAL_VAR
                    else -> BindingSourceType.JSON_FILE
                }
            }
            else -> BindingSourceType.JSON_FILE
        }

        return DataBinding(
            sourceType = sourceType,
            sourceName = sourceName,
            path = path,
            expression = "",
            defaultValue = ""
        )
    }

    /**
     * Извлекает значение из данных по пути
     */
    fun extractValue(data: Any?, path: String): Any? {
        if (data == null || path.isEmpty()) return data

        var current: Any? = data

        for (part in splitPath(path)) {
            current = extractValueFromPart(current, part)
            if (current == null) break
        }

        return current
    }

    /**
     * Разбивает путь на части, учитывая индексы массивов
     */
    private fun splitPath(path: String): List<String> {
        val parts = mutableListOf<String>()
        var current = StringBuilder()
        var inBrackets = false

        for (char in path) {
            when {
                char == '[' -> {
                    if (current.isNotEmpty()) {
                        parts.add(current.toString())
                        current.clear()
                    }
                    current.append(char)
                    inBrackets = true
                }
                char == ']' -> {
                    current.append(char)
                    parts.add(current.toString())
                    current.clear()
                    inBrackets = false
                }
                char == '.' && !inBrackets -> {
                    if (current.isNotEmpty()) {
                        parts.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
        }

        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }

        return parts
    }

    private fun extractValueFromPart(data: Any?, part: String): Any? {
        return when (data) {
            is JSONArray -> extractFromJSONArray(data, part)
            is JSONObject -> extractFromJSONObject(data, part)
            is List<*> -> extractFromList(data, part)
            is Map<*, *> -> extractFromMap(data, part)
            else -> extractFromObject(data, part)
        }
    }

    private fun extractFromJSONArray(jsonArray: JSONArray, part: String): Any? {
        if (part.startsWith("[") && part.endsWith("]")) {
            val index = part.substring(1, part.length - 1).toIntOrNull()
            return index?.takeIf { it < jsonArray.length() }?.let { jsonArray.opt(it) }
        }
        return null
    }

    private fun extractFromJSONObject(jsonObject: JSONObject, part: String): Any? {
        if (part.startsWith("[") && part.endsWith("]")) {
            val index = part.substring(1, part.length - 1).toIntOrNull()
            return index?.takeIf { index < jsonObject.length() }?.let {
                jsonObject.opt(jsonObject.names()?.optString(index))
            }
        }
        return jsonObject.opt(part)
    }

    private fun extractFromList(list: List<*>, part: String): Any? {
        if (part.startsWith("[") && part.endsWith("]")) {
            val index = part.substring(1, part.length - 1).toIntOrNull()
            return index?.takeIf { it < list.size }?.let { list[it] }
        }
        return null
    }

    private fun extractFromMap(map: Map<*, *>, part: String): Any? {
        return map[part]
    }

    private fun extractFromObject(obj: Any?, part: String): Any? {
        if (obj == null) return null

        return try {
            // Рефлексия для объектов
            val field = obj::class.java.getDeclaredField(part)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Заменяет биндинги в тексте реальными значениями
     */
    fun replaceBindings(text: String, bindings: List<DataBinding>, dataContext: DataContext): String {
        var result = text

        bindings.forEach { binding ->
            val value = resolveBinding(binding, dataContext)?.toString() ?: binding.defaultValue
            result = result.replace(binding.expression, value)
        }

        return result
    }

    /**
     * Разрешает биндинг в значение
     */
    private fun resolveBinding(binding: DataBinding, context: DataContext): Any? {
        Log.d(TAG, "Resolving binding: $binding")

        val sourceData = when (binding.sourceType) {
            BindingSourceType.JSON_FILE -> {
                Log.d(TAG, "Looking for JSON source: ${binding.sourceName}")
                val source = context.jsonSources[binding.sourceName]
                Log.d(TAG, "Found source: ${if (source != null) "non-null" else "null"}")
                source
            }
            else -> null
        }

        val result = if (binding.path.isNotEmpty()) {
            val value = extractValue(sourceData, binding.path)
            Log.d(TAG, "Extracted value for path '${binding.path}': $value")
            value
        } else {
            Log.d(TAG, "No path, returning source: $sourceData")
            sourceData
        }

        Log.d(TAG, "Final result: $result")
        return result
    }

    /**
     * Упрощенный метод для быстрого извлечения значения из JSON
     */
    fun extractFromJson(json: String, path: String): Any? {
        return try {
            val jsonObject = JSONObject(json)
            extractValue(jsonObject, path)
        } catch (e: Exception) {
            try {
                val jsonArray = JSONArray(json)
                extractValue(jsonArray, path)
            } catch (e2: Exception) {
                null
            }
        }
    }
}