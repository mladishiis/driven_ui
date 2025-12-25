package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.BindingSourceType
import com.example.drivenui.parser.models.DataBinding

/**
 * Парсер биндингов для извлечения макросов вида ${source.path}
 */
class BindingParser {

    companion object {
        private const val TAG = "BindingParser"
    }

    /**
     * Парсит строку и извлекает все биндинги
     */
    fun parseBindings(value: String): List<DataBinding> {
        if (value.isEmpty()) return emptyList()

        val bindings = mutableListOf<DataBinding>()
        val regex = Regex("""\$\{(.*?)\}""")

        regex.findAll(value).forEachIndexed { index, match ->
            try {
                val expression = match.groups[1]?.value ?: ""
                val binding = parseBindingExpression(expression)
                bindings.add(binding)
                Log.d(TAG, "Найден биндинг $index: $expression")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при парсинге биндинга: ${match.value}", e)
            }
        }

        Log.d(TAG, "Всего найдено биндингов в '$value': ${bindings.size}")
        return bindings
    }

    /**
     * Парсит выражение биндинга
     */
    private fun parseBindingExpression(expression: String): DataBinding {
        // Примеры выражений:
        // - carriers_allCarriers.[0].carrierName
        // - query_result.users[1].name
        // - app_state.currentUser.email

        // Определяем тип источника
        val sourceType = detectSourceType(expression)

        // Парсим имя источника и путь
        val (sourceName, path) = parseSourceAndPath(expression)

        return DataBinding(
            sourceType = sourceType,
            sourceName = sourceName,
            path = path,
            expression = "\${$expression}",
            defaultValue = ""
        )
    }

    /**
     * Определяет тип источника данных по выражению
     */
    private fun detectSourceType(expression: String): BindingSourceType {
        return when {
            // JSON файлы (например, carriers_allCarriers)
            expression.contains("_allCarriers") ||
                    expression.contains("_list") ||
                    expression.endsWith("Data") -> BindingSourceType.JSON_FILE

            // Результаты запросов
            expression.contains("query_") ||
                    expression.contains("api_") ||
                    expression.startsWith("result_") -> BindingSourceType.QUERY_RESULT

            // Локальные переменные (начинаются с @)
            expression.startsWith("@") -> BindingSourceType.LOCAL_VAR

            // Состояние приложения (общие переменные)
            expression.contains("app_") ||
                    expression.contains("state_") ||
                    expression.contains("global_") -> BindingSourceType.APP_STATE

            // По умолчанию - состояние приложения
            else -> BindingSourceType.APP_STATE
        }
    }

    /**
     * Разделяет выражение на имя источника и путь к данным
     */
    private fun parseSourceAndPath(expression: String): Pair<String, String> {
        // Ищем первую точку, которая разделяет sourceName и path
        val dotIndex = expression.indexOf('.')

        return if (dotIndex > 0) {
            // Есть путь: carriers_allCarriers.[0].carrierName
            val sourceName = expression.substring(0, dotIndex).trim()
            val path = expression.substring(dotIndex + 1).trim()
            sourceName to path
        } else {
            // Нет пути: carriers_allCarriers
            expression.trim() to ""
        }
    }

    /**
     * Проверяет, содержит ли строка биндинги
     */
    fun hasBindings(value: String): Boolean {
        return value.contains("\${")
    }

    /**
     * Извлекает имена всех источников из строки
     */
    fun extractSourceNames(value: String): List<String> {
        return parseBindings(value).map { it.sourceName }.distinct()
    }
}