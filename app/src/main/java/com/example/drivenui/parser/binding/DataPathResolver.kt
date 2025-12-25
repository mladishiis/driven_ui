package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.DataContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Разрешитель путей данных для извлечения значений из JSON структур
 */
object DataPathResolver {

    private const val TAG = "DataPathResolver"

    /**
     * Разрешает путь данных из контекста
     */
    fun resolve(context: DataContext, path: String): Any? {
        if (path.isEmpty()) return null

        Log.d(TAG, "Начинаем разрешение пути: '$path'")

        // Парсим путь на части
        val parts = parseFullPath(path)
        if (parts.isEmpty()) {
            Log.w(TAG, "Пустой путь: $path")
            return null
        }

        val sourceName = parts.first()
        val dataPath = if (parts.size > 1) {
            parts.drop(1).joinToString(".")
        } else {
            ""
        }

        Log.d(TAG, "Разделили путь: source='$sourceName', dataPath='$dataPath'")

        // Ищем данные в источниках
        val data = findInSources(context, sourceName)
        if (data == null) {
            Log.w(TAG, "Источник не найден: $sourceName")
            return null
        }

        // Если путь пустой, возвращаем весь объект
        if (dataPath.isEmpty()) {
            Log.d(TAG, "Путь пустой, возвращаем весь объект")
            return data
        }

        // Извлекаем данные по пути
        return extractByPath(data, dataPath)
    }

    /**
     * Ищет данные в источниках контекста
     */
    private fun findInSources(context: DataContext, sourceName: String): Any? {
        // Проверяем все типы источников по порядку приоритета
        val sources = listOf(
            context.jsonSources,
            context.queryResults,
            context.appState,
            context.localVariables
        )

        val sourceNames = listOf("jsonSources", "queryResults", "appState", "localVariables")

        sources.zip(sourceNames).forEach { (source, name) ->
            if (source.containsKey(sourceName)) {
                val value = source[sourceName]
                Log.d(TAG, "Найден в $name: $sourceName → ${value?.javaClass?.simpleName}")
                return value
            }
        }

        Log.w(TAG, "Источник не найден ни в одном контексте: $sourceName")
        Log.d(TAG, "Доступные источники:")
        sources.forEachIndexed { index, source ->
            Log.d(TAG, "  ${sourceNames[index]}: ${source.keys}")
        }
        return null
    }

    /**
     * Извлекает данные по пути из объекта
     */
    private fun extractByPath(data: Any, path: String): Any? {
        return try {
            Log.d(TAG, "Извлекаем данные по пути: '$path' из типа ${data.javaClass.simpleName}")

            val parts = parseJsonPath(path)
            var current: Any? = data

            for ((index, part) in parts.withIndex()) {
                Log.d(TAG, "Шаг $index: part='$part' (${part.javaClass.simpleName}), " +
                        "current=${current?.javaClass?.simpleName}")

                current = when (val currentValue = current) {
                    is JSONObject -> extractFromJsonObject(currentValue, part)
                    is JSONArray -> extractFromJsonArray(currentValue, part)
                    is Map<*, *> -> extractFromMap(currentValue, part)
                    is List<*> -> extractFromList(currentValue, part)
                    else -> {
                        Log.w(TAG, "Неизвестный тип данных на шаге $index: " +
                                "${currentValue?.javaClass?.simpleName}")
                        return null
                    }
                }

                if (current == null) {
                    Log.w(TAG, "Путь прерван на шаге $index: part='$part'")
                    return null
                }
            }

            Log.d(TAG, "Успешно извлечено значение: $current (${current?.javaClass?.simpleName})")
            current
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при извлечении данных по пути: '$path'", e)
            null
        }
    }

    /**
     * Извлекает данные из JSONObject
     */
    private fun extractFromJsonObject(jsonObject: JSONObject, part: Any): Any? {
        return if (part is String) {
            if (jsonObject.has(part)) {
                jsonObject.opt(part)
            } else {
                Log.w(TAG, "JSONObject не содержит ключ: '$part'. Доступные ключи: ${jsonObject.keys().asSequence().toList()}")
                null
            }
        } else {
            Log.w(TAG, "Для JSONObject ожидалась строка, получено: $part (${part.javaClass.simpleName})")
            null
        }
    }

    /**
     * Извлекает данные из JSONArray
     */
    private fun extractFromJsonArray(jsonArray: JSONArray, part: Any): Any? {
        return if (part is Int) {
            if (part in 0 until jsonArray.length()) {
                jsonArray.opt(part)
            } else {
                Log.w(TAG, "Индекс вне диапазона: $part, размер массива: ${jsonArray.length()}")
                null
            }
        } else {
            Log.w(TAG, "Для JSONArray ожидался Int, получено: $part (${part.javaClass.simpleName})")
            null
        }
    }

    /**
     * Извлекает данные из Map
     */
    private fun extractFromMap(map: Map<*, *>, part: Any): Any? {
        return if (part is String) {
            map[part]
        } else {
            Log.w(TAG, "Для Map ожидалась строка, получено: $part (${part.javaClass.simpleName})")
            null
        }
    }

    /**
     * Извлекает данные из List
     */
    private fun extractFromList(list: List<*>, part: Any): Any? {
        return if (part is Int) {
            if (part in 0 until list.size) {
                list[part]
            } else {
                Log.w(TAG, "Индекс вне диапазона: $part, размер списка: ${list.size}")
                null
            }
        } else {
            Log.w(TAG, "Для List ожидался Int, получено: $part (${part.javaClass.simpleName})")
            null
        }
    }

    /**
     * Парсит полный путь (например, "sourceName.[0].field.subfield")
     */
    private fun parseFullPath(fullPath: String): List<String> {
        return fullPath.split('.').map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Парсит JSON путь на части (строки и индексы)
     */
    private fun parseJsonPath(path: String): List<Any> {
        val parts = mutableListOf<Any>()
        var current = StringBuilder()
        var inBrackets = false

        for (char in path) {
            when {
                char == '[' -> {
                    if (current.isNotEmpty()) {
                        parts.add(current.toString())
                        current.clear()
                    }
                    inBrackets = true
                    current.append('[')
                }

                char == ']' -> {
                    if (inBrackets) {
                        val bracketContent = current.toString()
                        if (bracketContent.startsWith('[')) {
                            val indexStr = bracketContent.substring(1)
                            try {
                                val indexValue = indexStr.toInt()
                                parts.add(indexValue)
                            } catch (e: NumberFormatException) {
                                Log.w(TAG, "Неверный индекс массива: '$indexStr'")
                                parts.add(indexStr)
                            }
                        }
                        current.clear()
                        inBrackets = false
                    }
                }

                char == '.' && !inBrackets -> {
                    if (current.isNotEmpty()) {
                        parts.add(current.toString())
                        current.clear()
                    }
                }

                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }

        Log.d(TAG, "Разобран путь '$path' на части: $parts")
        return parts
    }

    /**
     * Преобразует значение в строку для подстановки
     */
    fun valueToString(value: Any?): String {
        return when (value) {
            null -> {
                Log.d(TAG, "Преобразование null в пустую строку")
                ""
            }
            is String -> {
                Log.d(TAG, "Преобразование строки: '$value'")
                value
            }
            is Number -> {
                Log.d(TAG, "Преобразование числа: $value")
                value.toString()
            }
            is Boolean -> {
                Log.d(TAG, "Преобразование boolean: $value")
                value.toString()
            }
            is JSONObject -> {
                Log.d(TAG, "Преобразование JSONObject: ${value.toString().take(50)}...")
                value.toString()
            }
            is JSONArray -> {
                Log.d(TAG, "Преобразование JSONArray длиной ${value.length()}")
                value.toString()
            }
            else -> {
                Log.d(TAG, "Преобразование неизвестного типа ${value.javaClass.simpleName}: $value")
                value.toString()
            }
        }
    }

    /**
     * Разрешает путь и преобразует в строку (удобный метод)
     */
    fun resolveToString(context: DataContext, path: String): String {
        val result = resolve(context, path)
        return valueToString(result)
    }
}