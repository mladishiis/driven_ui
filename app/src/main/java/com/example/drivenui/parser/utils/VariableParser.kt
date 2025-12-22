package com.example.drivenui.parser.utils

/**
 * Парсер переменных Driven UI
 *
 * Поддерживает все типы переменных:
 * - Контекстные переменные: @{microapp.name}
 * - Глобальные переменные: @@{name}
 * - Переменные ответов: ${query.path}
 * - Литеральные значения
 */
object VariableParser {

    /**
     * Тип переменной Driven UI
     */
    sealed class VariableType {
        /**
         * Контекстная переменная микроаппа
         * @property microappCode Код микроаппа
         * @property name Имя переменной в контексте микроаппа
         */
        data class ContextVariable(val microappCode: String, val name: String) : VariableType()

        /**
         * Глобальная переменная
         * @property name Имя глобальной переменной
         */
        data class GlobalVariable(val name: String) : VariableType()

        /**
         * Переменная из ответа запроса
         * @property queryName Имя запроса
         * @property path Путь к данным в ответе (например, "accounts[0].balance")
         */
        data class ResponseVariable(val queryName: String, val path: String) : VariableType()

        /**
         * Литеральное значение
         * @property value Значение литерала
         */
        data class Literal(val value: String) : VariableType()
    }

    /**
     * Определяет тип переменной по выражению
     *
     * @param expression Выражение для анализа (может быть контекстной, глобальной переменной,
     *                   переменной ответа или литералом)
     * @return [VariableType] тип переменной
     *
     * @example parseVariable("@{microappVTB.balance}") -> ContextVariable
     * @example parseVariable("@@{globalSetting}") -> GlobalVariable
     * @example parseVariable("\${query.accounts[0].balance}") -> ResponseVariable
     * @example parseVariable("Hello World") -> Literal
     */
    fun parseVariable(expression: String): VariableType {
        return when {
            expression.startsWith("@{") && expression.endsWith("}") -> {
                parseContextVariable(expression)
            }
            expression.startsWith("@@{") && expression.endsWith("}") -> {
                parseGlobalVariable(expression)
            }
            expression.startsWith("\${") && expression.endsWith("}") -> {
                parseResponseVariable(expression)
            }
            else -> VariableType.Literal(expression)
        }
    }

    /**
     * Парсит контекстную переменную
     *
     * @param expression Выражение в формате @{microappCode.name}
     * @return [VariableType.ContextVariable] если парсинг успешен, иначе [VariableType.Literal]
     */
    private fun parseContextVariable(expression: String): VariableType {
        val content = expression.removePrefix("@").removeSurrounding("{", "}")
        val parts = content.split(".", limit = 2)
        return if (parts.size == 2) {
            VariableType.ContextVariable(parts[0], parts[1])
        } else {
            VariableType.Literal(expression)
        }
    }

    /**
     * Парсит глобальную переменную
     *
     * @param expression Выражение в формате @@{name}
     * @return [VariableType.GlobalVariable]
     */
    private fun parseGlobalVariable(expression: String): VariableType {
        val content = expression.removePrefix("@@").removeSurrounding("{", "}")
        return VariableType.GlobalVariable(content)
    }

    /**
     * Парсит переменную из ответа запроса
     *
     * @param expression Выражение в формате ${queryName.path}
     * @return [VariableType.ResponseVariable] если парсинг успешен, иначе [VariableType.Literal]
     */
    private fun parseResponseVariable(expression: String): VariableType {
        val content = expression.removeSurrounding("\${", "}")
        val parts = content.split(".", limit = 2)
        return if (parts.size == 2) {
            VariableType.ResponseVariable(parts[0], parts[1])
        } else {
            VariableType.Literal(expression)
        }
    }

    /**
     * Вычисляет значение выражения с учетом контекста
     *
     * @param expression Выражение для вычисления
     * @param context Карта значений переменных в контексте
     * @return Значение выражения или null если не удалось вычислить
     */
    fun evaluateExpression(expression: String, context: Map<String, Any>): Any? {
        return try {
            when (val variable = parseVariable(expression)) {
                is VariableType.ContextVariable -> {
                    context["${variable.microappCode}.${variable.name}"]
                }
                is VariableType.GlobalVariable -> {
                    // Глобальные переменные из shared preferences или других хранилищ
                    null
                }
                is VariableType.ResponseVariable -> {
                    // Данные из ответа запроса (требует доступа к кэшу запросов)
                    null
                }
                is VariableType.Literal -> variable.value
            }
        } catch (e: Exception) {
            null
        }
    }
}