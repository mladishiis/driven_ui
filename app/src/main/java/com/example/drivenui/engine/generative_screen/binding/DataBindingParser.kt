package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.engine.parser.models.BindingSourceType
import com.example.drivenui.engine.parser.models.DataBinding
import com.example.drivenui.engine.parser.models.DataContext
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

object DataBindingParser {

    private const val TAG = "DataBindingParser"

    /**
     * Индекс первой capturing group в [MatchResult.groupValues]:
     * `[0]` — полное совпадение, `[1]` — первые скобки `(...)` в regex.
     */
    private const val REGEX_FIRST_CAPTURE_GROUP_INDEX = 1

    /** Индекс элемента массива в пути: только `[0]`, `[12]`, не `[{#i}]`. */
    private val ARRAY_INDEX_PREFIX = Regex("""^\[(\d+)]""")

    /**
     * Парсит строку с макросами ${...} на список биндингов.
     *
     * @param text строка, содержащая выражения вида ${source.path}
     * @return список распарсенных [DataBinding]
     */
    fun parseBindings(text: String): List<DataBinding> {
        val pattern = "\\\$\\{([^}]+)\\}".toRegex()
        val bindings = mutableListOf<DataBinding>()

        pattern.findAll(text).forEach { matchResult ->
            val expression = matchResult.value
            val content = matchResult.groupValues[REGEX_FIRST_CAPTURE_GROUP_INDEX]

            parseBinding(content)?.let { binding ->
                val fullBinding = binding.copy(expression = expression)
                bindings.add(fullBinding)
            }
        }

        return bindings
    }

    /**
     * Парсит одиночное выражение биндинга.
     *
     * Имя источника — всегда первый сегмент до первой точки или скобки
     * (код запроса, имя JSON-файла и т.п.). Остальное — путь внутри источника.
     */
    private fun parseBinding(content: String): DataBinding? {
        val sourceEnd = content.indexOfFirst { it == '.' || it == '[' }
        if (sourceEnd == -1) {
            return DataBinding(
                sourceType = detectSourceType(content),
                sourceName = content,
                path = "",
                expression = "",
                defaultValue = "",
            )
        }

        val sourceName = content.substring(0, sourceEnd)
        val rest = content.substring(sourceEnd)
        val path = if (rest.startsWith(".")) rest.drop(1) else rest

        return DataBinding(
            sourceType = detectSourceType(sourceName),
            sourceName = sourceName,
            path = path,
            expression = "",
            defaultValue = "",
        )
    }

    /**
     * Определяет тип источника по его имени
     */
    private fun detectSourceType(sourceName: String): BindingSourceType {
        return when {
            sourceName.matches(Regex("^[a-z]+_[a-zA-Z]+$")) -> BindingSourceType.SCREEN_QUERY_RESULT
            sourceName.endsWith("_json") -> BindingSourceType.JSON_FILE
            sourceName.endsWith(".json") -> BindingSourceType.JSON_FILE
            else -> BindingSourceType.SCREEN_QUERY_RESULT
        }
    }

    /**
     * Встроенная операция движка: размер JSON-массива (не поле объекта в ответе API).
     */
    private fun arrayCountElement(array: JsonArray): JsonPrimitive =
        JsonPrimitive(array.size())

    private fun navigateProperty(current: JsonElement, property: String): JsonElement? =
        when {
            property == "count" && current is JsonArray -> arrayCountElement(current)
            current is JsonObject -> current.get(property)
            else -> null
        }

    /**
     * Извлекает значение из JsonElement по пути (поддерживает [index], .property и .count для массивов).
     *
     * @param data JSON-элемент для обхода
     * @param path путь в формате [0].field, array.count или field.subfield
     * @return извлечённое значение или null
     */
    fun extractValue(data: JsonElement?, path: String): Any? {
        if (data == null || path.isEmpty()) return data?.toString()

        var current: JsonElement? = data
        var remainingPath = path

        while (remainingPath.isNotEmpty() && current != null) {
            if (remainingPath.startsWith('[')) {
                val indexMatch = ARRAY_INDEX_PREFIX.find(remainingPath)
                if (indexMatch == null) {
                    Log.w(TAG, "Ожидался числовой индекс массива в пути: $remainingPath")
                    return null
                }
                val index = indexMatch.groupValues[REGEX_FIRST_CAPTURE_GROUP_INDEX].toInt()

                if (current is JsonArray) {
                    current = if (index < current.size()) current[index] else null
                } else if (current is JsonObject) {
                    var found = false
                    for ((_, value) in current.entrySet()) {
                        if (value is JsonArray) {
                            current = if (index < value.size()) value[index] else null
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        Log.w(TAG, "В объекте не найден массив для индекса $index")
                        return null
                    }
                } else {
                    Log.w(TAG, "Нет доступа к индексу $index у $current")
                    return null
                }

                remainingPath = remainingPath.substring(indexMatch.range.last + 1)
                if (remainingPath.startsWith('.')) {
                    remainingPath = remainingPath.substring(1)
                }
            }
            else if (remainingPath.contains('.')) {
                val dotIndex = remainingPath.indexOf('.')
                val property = remainingPath.substring(0, dotIndex)

                val parent = current!!
                current = navigateProperty(parent, property)
                if (current == null) {
                    Log.w(TAG, "Нет свойства '$property' у $parent")
                    return null
                }

                remainingPath = remainingPath.substring(dotIndex + 1)
            }
            else {
                val property = remainingPath
                val parent = current!!
                current = when {
                    property == "count" && parent is JsonArray -> arrayCountElement(parent)
                    parent is JsonObject -> parent.get(property)
                    parent is JsonArray && property.toIntOrNull() != null -> {
                        val index = property.toInt()
                        if (index < parent.size()) parent[index] else null
                    }
                    else -> null
                }
                if (current == null) {
                    Log.w(TAG, "Нет свойства '$property' у $parent")
                    return null
                }
                remainingPath = ""
            }
        }

        val result = when (current) {
            is JsonObject -> current.toString()
            is JsonArray -> current.toString()
            is JsonElement -> if (current.isJsonPrimitive) current.asString else current.toString()
            else -> null
        }

        return result
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
        val sourceData: JsonElement? = when (binding.sourceType) {
            BindingSourceType.SCREEN_QUERY_RESULT -> {
                val result = context.screenQueryResults[binding.sourceName]
                when (result) {
                    is JsonElement -> {
                        result
                    }
                    is String -> {
                        try {
                            JsonParser.parseString(result)
                        } catch (e: Exception) {
                            Log.e(TAG, "Не удалось разобрать строку как JSON: $result")
                            null
                        }
                    }
                    else -> {
                        Log.w(TAG, "Неизвестный тип данных результата запроса экрана: ${result?.javaClass?.simpleName}")
                        null
                    }
                }
            }
            BindingSourceType.JSON_FILE -> {
                context.jsonSources[binding.sourceName]
            }
            else -> null
        }

        if (sourceData == null) {
            Log.w(TAG, "Нет данных источника для: ${binding.sourceName}")
            return null
        }

        val result = if (binding.path.isNotEmpty()) {
            if (binding.path == "count" && sourceData is JsonArray) {
                sourceData.size()
            } else {
                extractValue(sourceData, binding.path)
            }
        } else {
            when (sourceData) {
                is JsonObject, is JsonArray -> sourceData.toString()
                else -> sourceData.toString()
            }
        }

        return result
    }
}