package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.engine.parser.models.BindingSourceType
import com.example.drivenui.engine.parser.models.DataBinding
import com.example.drivenui.engine.parser.models.DataContext
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object DataBindingParser {

    private const val TAG = "DataBindingParser"

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
            val content = matchResult.groupValues[1]

            parseBinding(content)?.let { binding ->
                val fullBinding = binding.copy(expression = expression)
                bindings.add(fullBinding)
            }
        }

        return bindings
    }

    /**
     * Парсит одиночное выражение биндинга. Поддерживаемые форматы:
     * - source.[index].property (например carriers_allCarriers.[0].carrierName)
     * - source[index].property (например carriers_allCarriers[0].carrierName)
     * - source.property (точечная нотация)
     * - source (только имя источника)
     */
    private fun parseBinding(content: String): DataBinding? {
        if (content.contains(".[")) {
            val dotBracketIndex = content.indexOf(".[")
            val sourceName = content.substring(0, dotBracketIndex)
            val rest = content.substring(dotBracketIndex + 1)

            return DataBinding(
                sourceType = detectSourceType(sourceName),
                sourceName = sourceName,
                path = rest,
                expression = "",
                defaultValue = ""
            )
        }
        else if (content.contains("[")) {
            val bracketIndex = content.indexOf("[")
            val sourceName = content.substring(0, bracketIndex)
            val rest = content.substring(bracketIndex)

            return DataBinding(
                sourceType = detectSourceType(sourceName),
                sourceName = sourceName,
                path = rest,
                expression = "",
                defaultValue = ""
            )
        }
        else if (content.contains(".")) {
            val dotIndex = content.indexOf(".")
            val sourceName = content.substring(0, dotIndex)
            val path = content.substring(dotIndex + 1)

            return DataBinding(
                sourceType = detectSourceType(sourceName),
                sourceName = sourceName,
                path = path,
                expression = "",
                defaultValue = ""
            )
        }
        else {
            return DataBinding(
                sourceType = detectSourceType(content),
                sourceName = content,
                path = "",
                expression = "",
                defaultValue = ""
            )
        }
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
     * Извлекает значение из JsonElement по пути (поддерживает [index] и .property).
     *
     * @param data JSON-элемент для обхода
     * @param path путь в формате [0].property или .property
     * @return извлечённое значение или null
     */
    fun extractValue(data: JsonElement?, path: String): Any? {
        if (data == null || path.isEmpty()) return data?.toString()

        var current: JsonElement? = data
        var remainingPath = path

        while (remainingPath.isNotEmpty() && current != null) {
            if (remainingPath.startsWith('[') && remainingPath.contains(']')) {
                val endIndex = remainingPath.indexOf(']')
                val indexStr = remainingPath.substring(1, endIndex)
                val index = indexStr.toIntOrNull()

                if (current is JsonArray && index != null) {
                    current = if (index < current.size()) current[index] else null
                } else if (current is JsonObject) {
                    val entrySet = current.entrySet()
                    var found = false

                    for ((key, value) in entrySet) {
                        if (value is JsonArray && index != null) {
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
                    Log.w(TAG, "Нет доступа к индексу $indexStr у $current")
                    return null
                }

                remainingPath = if (endIndex + 1 < remainingPath.length) {
                    remainingPath.substring(endIndex + 1)
                } else {
                    ""
                }

                if (remainingPath.startsWith('.')) {
                    remainingPath = remainingPath.substring(1)
                }
            }
            else if (remainingPath.contains('.')) {
                val dotIndex = remainingPath.indexOf('.')
                val property = remainingPath.substring(0, dotIndex)

                if (current is JsonObject) {
                    current = current.get(property)
                } else {
                    Log.w(TAG, "Нет свойства '$property' у $current")
                    return null
                }

                remainingPath = remainingPath.substring(dotIndex + 1)
            }
            else {
                val property = remainingPath
                if (current is JsonObject) {
                    current = current.get(property)
                } else if (current is JsonArray && property.toIntOrNull() != null) {
                    val index = property.toInt()
                    current = if (index < current.size()) current[index] else null
                } else {
                    Log.w(TAG, "Нет свойства '$property' у $current")
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