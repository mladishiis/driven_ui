package com.example.drivenui.engine.generative_screen.binding

import android.util.Log
import com.example.drivenui.parser.models.DataBinding
import com.example.drivenui.parser.models.BindingSourceType
import com.example.drivenui.parser.models.DataContext
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

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
            } ?: run {
                Log.d(TAG, "Failed to parse binding: '$content'")
            }
        }

        return bindings
    }

    /**
     * Парсит одиночное выражение биндинга
     */
    private fun parseBinding(content: String): DataBinding? {
        Log.d(TAG, "Parsing binding content: '$content'")

        // Поддерживаем два формата:
        // 1. carriers_allCarriers.[0].carrierName (с точкой перед скобками)
        // 2. carriers_allCarriers[0].carrierName (без точки)

        // Если есть точка перед скобкой
        if (content.contains(".[")) {
            // Формат: source.[index].property
            val dotBracketIndex = content.indexOf(".[")
            val sourceName = content.substring(0, dotBracketIndex)
            val rest = content.substring(dotBracketIndex + 1) // +1 чтобы пропустить точку

            // rest теперь: [0].carrierName

            return DataBinding(
                sourceType = detectSourceType(sourceName),
                sourceName = sourceName,
                path = rest,
                expression = "",
                defaultValue = ""
            )
        }
        // Если есть просто скобка
        else if (content.contains("[")) {
            // Формат: source[index].property
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
        // Простая точечная нотация
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
        // Просто имя источника без пути
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
            // ScreenQuery коды (из XML файла)
            sourceName.matches(Regex("^[a-z]+_[a-zA-Z]+$")) -> BindingSourceType.SCREEN_QUERY_RESULT
            sourceName.endsWith("_json") -> BindingSourceType.JSON_FILE
            sourceName.endsWith(".json") -> BindingSourceType.JSON_FILE
            else -> BindingSourceType.SCREEN_QUERY_RESULT // По умолчанию
        }
    }

    /**
     * Извлекает значение из JsonElement по пути
     */
    /**
     * Извлекает значение из JsonElement по пути
     */
    fun extractValue(data: JsonElement?, path: String): Any? {
        if (data == null || path.isEmpty()) return data?.toString()

        Log.d(TAG, "Extracting value: path='$path', data type: ${data::class.simpleName}")

        var current: JsonElement? = data
        var remainingPath = path

        while (remainingPath.isNotEmpty() && current != null) {
            Log.d(TAG, "Current: $current, remaining: '$remainingPath'")

            // Пробуем извлечь индекс массива
            if (remainingPath.startsWith('[') && remainingPath.contains(']')) {
                val endIndex = remainingPath.indexOf(']')
                val indexStr = remainingPath.substring(1, endIndex)
                val index = indexStr.toIntOrNull()

                if (current is JsonArray && index != null) {
                    current = if (index < current.size()) current[index] else null
                    Log.d(TAG, "Accessed array at index $index: $current")
                } else if (current is JsonObject) {
                    // Если текущий объект, ищем в нем свойство с именем как источник
                    Log.d(TAG, "Trying to find array in JsonObject")

                    // Проверяем, есть ли у объекта свойство с массивом
                    val entrySet = current.entrySet()
                    var found = false

                    for ((key, value) in entrySet) {
                        if (value is JsonArray && index != null) {
                            current = if (index < value.size()) value[index] else null
                            found = true
                            Log.d(TAG, "Found array in property '$key' at index $index")
                            break
                        }
                    }

                    if (!found) {
                        Log.w(TAG, "No array found in object for index $index")
                        return null
                    }
                } else {
                    Log.w(TAG, "Cannot access index $indexStr on $current")
                    return null
                }

                // Удаляем обработанную часть
                remainingPath = if (endIndex + 1 < remainingPath.length) {
                    remainingPath.substring(endIndex + 1)
                } else {
                    ""
                }

                // Если следующий символ - точка, пропускаем её
                if (remainingPath.startsWith('.')) {
                    remainingPath = remainingPath.substring(1)
                }
            }

            // Обработка свойства объекта
            else if (remainingPath.contains('.')) {
                val dotIndex = remainingPath.indexOf('.')
                val property = remainingPath.substring(0, dotIndex)

                if (current is JsonObject) {
                    current = current.get(property)
                    Log.d(TAG, "Accessed property '$property': $current")
                } else {
                    Log.w(TAG, "Cannot get property '$property' from $current")
                    return null
                }

                remainingPath = remainingPath.substring(dotIndex + 1)
            }

            // Последняя часть пути
            else {
                val property = remainingPath
                if (current is JsonObject) {
                    current = current.get(property)
                    Log.d(TAG, "Accessed final property '$property': $current")
                } else if (current is JsonArray && property.toIntOrNull() != null) {
                    val index = property.toInt()
                    current = if (index < current.size()) current[index] else null
                    Log.d(TAG, "Accessed array at final index $index: $current")
                } else {
                    Log.w(TAG, "Cannot get property '$property' from $current")
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

        Log.d(TAG, "Extracted value: $result")
        return result
    }

    /**
     * Заменяет биндинги в тексте реальными значениями
     */
    fun replaceBindings(text: String, bindings: List<DataBinding>, dataContext: DataContext): String {
        var result = text

        bindings.forEach { binding ->
            val value = resolveBinding(binding, dataContext)?.toString() ?: binding.defaultValue
            Log.d(TAG, "Replacing '${binding.expression}' with '$value'")
            result = result.replace(binding.expression, value)
        }

        Log.d(TAG, "Original: '$text', Result: '$result'")
        return result
    }

    /**
     * Разрешает биндинг в значение
     */
    private fun resolveBinding(binding: DataBinding, context: DataContext): Any? {
        Log.d(TAG, "Resolving binding: $binding")

        val sourceData: JsonElement? = when (binding.sourceType) {
            BindingSourceType.SCREEN_QUERY_RESULT -> {
                Log.d(TAG, "Looking for screen query: ${binding.sourceName}")
                val result = context.screenQueryResults[binding.sourceName]
                when (result) {
                    is JsonElement -> {
                        Log.d(TAG, "Found JsonElement: ${result::class.simpleName}")
                        result
                    }
                    is String -> {
                        try {
                            JsonParser.parseString(result)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse string as JSON: $result")
                            null
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unknown data type for screen query: ${result?.javaClass?.simpleName}")
                        null
                    }
                }
            }
            BindingSourceType.JSON_FILE -> {
                Log.d(TAG, "Looking for JSON source: ${binding.sourceName}")
                val jsonElement = context.jsonSources[binding.sourceName]
                Log.d(TAG, "Found JSON source: ${jsonElement != null}")
                jsonElement
            }
            else -> null
        }

        if (sourceData == null) {
            Log.w(TAG, "No source data found for: ${binding.sourceName}")
            Log.d(TAG, "Available screen queries: ${context.screenQueryResults.keys}")
            Log.d(TAG, "Available JSON sources: ${context.jsonSources.keys}")
            return null
        }

        val result = if (binding.path.isNotEmpty()) {
            extractValue(sourceData, binding.path)
        } else {
            when (sourceData) {
                is JsonObject, is JsonArray -> sourceData.toString()
                else -> sourceData.toString()
            }
        }

        Log.d(TAG, "Final resolved result: $result")
        return result
    }
}