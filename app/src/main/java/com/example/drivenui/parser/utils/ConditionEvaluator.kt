package com.example.drivenui.parser.utils

/**
 * Вычислитель условий Driven UI
 *
 * Поддерживает оценку условий с операторами:
 * - Сравнения: ==, !=, >, >=, <, <=
 * - Логические: &&, ||
 * - Арифметические: +, -, *, /, %, //, ^
 * - Проверка вхождения: in
 */
object ConditionEvaluator {

    /**
     * Оценивает условие с учетом контекста переменных
     *
     * @param condition Выражение условия (например, "@{var}==true")
     * @param context Карта значений переменных в контексте
     * @return true если условие истинно, false если ложно или не удалось оценить
     */
    fun evaluateCondition(condition: String, context: Map<String, Any>): Boolean {
        return try {
            // Удаляем пробелы для упрощения парсинга
            val trimmedCondition = condition.replace("\\s+".toRegex(), "")

            // Парсим и оцениваем выражение
            evaluateExpression(trimmedCondition, context) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Рекурсивно оценивает выражение
     *
     * @param expression Выражение для оценки
     * @param context Карта значений переменных
     * @return Результат оценки выражения
     */
    private fun evaluateExpression(expression: String, context: Map<String, Any>): Any? {
        return when {
            // Булево значение
            expression == "true" -> true
            expression == "false" -> false

            // Числовое значение
            expression.matches(Regex("-?\\d+(\\.\\d+)?")) -> {
                if (expression.contains(".")) expression.toDouble() else expression.toInt()
            }

            // Строковое значение в кавычках
            expression.startsWith("\"") && expression.endsWith("\"") -> {
                expression.removeSurrounding("\"")
            }

            // Оператор in
            "in" in expression -> {
                evaluateInOperator(expression, context)
            }

            // Логические операторы (сначала &&, потом ||)
            "&&" in expression -> {
                evaluateLogicalOperator(expression, "&&", context)
            }
            "||" in expression -> {
                evaluateLogicalOperator(expression, "||", context)
            }

            // Операторы сравнения
            "==" in expression -> {
                evaluateComparison(expression, "==", context)
            }
            "!=" in expression -> {
                evaluateComparison(expression, "!=", context)
            }
            ">=" in expression -> {
                evaluateComparison(expression, ">=", context)
            }
            "<=" in expression -> {
                evaluateComparison(expression, "<=", context)
            }
            ">" in expression -> {
                evaluateComparison(expression, ">", context)
            }
            "<" in expression -> {
                evaluateComparison(expression, "<", context)
            }

            // Арифметические операторы
            "+" in expression -> {
                evaluateArithmetic(expression, "+", context)
            }
            "-" in expression -> {
                evaluateArithmetic(expression, "-", context)
            }
            "*" in expression -> {
                evaluateArithmetic(expression, "*", context)
            }
            "/" in expression -> {
                evaluateArithmetic(expression, "/", context)
            }
            "%" in expression -> {
                evaluateArithmetic(expression, "%", context)
            }
            "//" in expression -> {
                evaluateArithmetic(expression, "//", context)
            }
            "^" in expression -> {
                evaluateArithmetic(expression, "^", context)
            }

            // Скобки
            expression.startsWith("(") && expression.endsWith(")") -> {
                evaluateExpression(expression.removeSurrounding("(", ")"), context)
            }

            // Переменная
            else -> VariableParser.evaluateExpression(expression, context)
        }
    }

    /**
     * Оценивает оператор in
     *
     * @param expression Выражение с оператором in
     * @param context Контекст переменных
     * @return true если элемент содержится в списке
     */
    private fun evaluateInOperator(expression: String, context: Map<String, Any>): Boolean {
        val parts = expression.split("in")
        if (parts.size != 2) return false

        val left = evaluateExpression(parts[0], context)
        val right = evaluateExpression(parts[1], context)

        return when (right) {
            is List<*> -> right.contains(left)
            is Array<*> -> right.contains(left)
            is String -> right.contains(left.toString())
            else -> false
        }
    }

    /**
     * Оценивает логический оператор
     *
     * @param expression Выражение с логическим оператором
     * @param operator Оператор (&& или ||)
     * @param context Контекст переменных
     * @return Результат логической операции
     */
    private fun evaluateLogicalOperator(
        expression: String,
        operator: String,
        context: Map<String, Any>
    ): Boolean {
        val parts = splitByOperator(expression, operator)
        if (parts.size != 2) return false

        val left = evaluateExpression(parts[0], context) as? Boolean ?: false
        val right = evaluateExpression(parts[1], context) as? Boolean ?: false

        return when (operator) {
            "&&" -> left && right
            "||" -> left || right
            else -> false
        }
    }

    /**
     * Оценивает оператор сравнения
     *
     * @param expression Выражение с оператором сравнения
     * @param operator Оператор сравнения (==, !=, >, >=, <, <=)
     * @param context Контекст переменных
     * @return Результат сравнения
     */
    private fun evaluateComparison(
        expression: String,
        operator: String,
        context: Map<String, Any>
    ): Boolean {
        val parts = splitByOperator(expression, operator)
        if (parts.size != 2) return false

        val left = evaluateExpression(parts[0], context)
        val right = evaluateExpression(parts[1], context)

        return compareValues(left, right, operator)
    }

    /**
     * Сравнивает два значения с учетом типа
     *
     * @param left Левое значение
     * @param right Правое значение
     * @param operator Оператор сравнения
     * @return Результат сравнения
     */
    private fun compareValues(left: Any?, right: Any?, operator: String): Boolean {
        return when {
            left == null || right == null -> false
            left is Number && right is Number -> compareNumbers(left, right, operator)
            else -> compareObjects(left, right, operator)
        }
    }

    /**
     * Сравнивает числа
     *
     * @param left Левое число
     * @param right Правое число
     * @param operator Оператор сравнения
     * @return Результат сравнения чисел
     */
    private fun compareNumbers(left: Number, right: Number, operator: String): Boolean {
        val leftDouble = left.toDouble()
        val rightDouble = right.toDouble()

        return when (operator) {
            "==" -> leftDouble == rightDouble
            "!=" -> leftDouble != rightDouble
            ">" -> leftDouble > rightDouble
            ">=" -> leftDouble >= rightDouble
            "<" -> leftDouble < rightDouble
            "<=" -> leftDouble <= rightDouble
            else -> false
        }
    }

    /**
     * Сравнивает объекты
     *
     * @param left Левый объект
     * @param right Правый объект
     * @param operator Оператор сравнения
     * @return Результат сравнения объектов
     */
    private fun compareObjects(left: Any, right: Any, operator: String): Boolean {
        return when (operator) {
            "==" -> left == right
            "!=" -> left != right
            else -> false // Операторы >, < и т.д. не применимы к объектам
        }
    }

    /**
     * Оценивает арифметическую операцию
     *
     * @param expression Выражение с арифметическим оператором
     * @param operator Арифметический оператор (+, -, *, /, %, //, ^)
     * @param context Контекст переменных
     * @return Результат арифметической операции
     */
    private fun evaluateArithmetic(
        expression: String,
        operator: String,
        context: Map<String, Any>
    ): Number? {
        val parts = splitByOperator(expression, operator)
        if (parts.size != 2) return null

        val left = evaluateExpression(parts[0], context) as? Number
        val right = evaluateExpression(parts[1], context) as? Number

        if (left == null || right == null) return null

        return when (operator) {
            "+" -> left.toDouble() + right.toDouble()
            "-" -> left.toDouble() - right.toDouble()
            "*" -> left.toDouble() * right.toDouble()
            "/" -> left.toDouble() / right.toDouble()
            "%" -> left.toDouble() % right.toDouble()
            "//" -> left.toInt() / right.toInt()
            "^" -> Math.pow(left.toDouble(), right.toDouble())
            else -> null
        }
    }

    /**
     * Разделяет выражение по оператору, учитывая вложенные скобки
     *
     * @param expression Выражение для разделения
     * @param operator Оператор для разделения
     * @return Массив из двух частей выражения
     */
    private fun splitByOperator(expression: String, operator: String): List<String> {
        var bracketDepth = 0
        var operatorIndex = -1

        for (i in expression.indices) {
            when (expression[i]) {
                '(' -> bracketDepth++
                ')' -> bracketDepth--
                else -> {
                    if (bracketDepth == 0) {
                        // Проверяем, является ли текущая позиция началом оператора
                        if (expression.startsWith(operator, i)) {
                            operatorIndex = i
                            break
                        }
                    }
                }
            }
        }

        return if (operatorIndex != -1) {
            listOf(
                expression.substring(0, operatorIndex),
                expression.substring(operatorIndex + operator.length)
            )
        } else {
            emptyList()
        }
    }
}