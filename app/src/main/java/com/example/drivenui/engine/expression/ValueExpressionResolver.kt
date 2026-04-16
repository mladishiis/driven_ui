package com.example.drivenui.engine.expression

import com.example.drivenui.engine.context.IContextManager

/** Порядок разбора: сначала `@@{…}` (движок), затем `@{…}`. */
private val ENGINE_CONTEXT_VAR_PATTERN = Regex("@@\\{([^}]+)\\}")

/** `@{microappCode.variableName}` в любой позиции строки. */
private val MICROAPP_CONTEXT_VAR_PATTERN = Regex("@\\{([^}]+)\\}")

/**
 * Резолвит строковое значение с учётом условных выражений *if(...)*then(...)*else(...)
 * и подстановки переменных контекста @{microapp.var} и @@{var}.
 *
 * @param raw Исходная строка с выражениями
 * @param contextManager Менеджер контекста для доступа к переменным
 * @return Резолвленное значение
 */
fun resolveValueExpression(
    raw: String,
    contextManager: IContextManager
): String {
    parseConditionalExpression(raw)?.let { conditional ->
        val conditionResult = evalCondition(conditional.condition, contextManager)
        val branchRaw = if (conditionResult) conditional.thenBranch else conditional.elseBranch
        return resolveContextVariables(branchRaw, contextManager)
    }
    return resolveContextVariables(raw, contextManager)
}

private data class ConditionalExpression(
    val condition: String,
    val thenBranch: String,
    val elseBranch: String
)

/**
 * Парсит условное выражение *if(...)*then(...)*else(...).
 *
 * @param value Исходная строка
 * @return ConditionalExpression или null, если формат не совпадает
 */
private fun parseConditionalExpression(value: String): ConditionalExpression? {
    if (!value.startsWith("*if(")) return null
    val thenIndex = value.indexOf(")*then(")
    val elseIndex = value.indexOf(")*else(")
    if (thenIndex == -1 || elseIndex == -1 || elseIndex <= thenIndex) return null

    val condition = value.substring(4, thenIndex)
    val thenStart = thenIndex + ")*then(".length
    val thenEnd = value.indexOf(")", thenStart)
    if (thenEnd == -1 || thenEnd > elseIndex) return null
    val thenBranch = value.substring(thenStart, thenEnd)

    val elseStart = elseIndex + ")*else(".length
    val elseEnd = value.lastIndexOf(")")
    if (elseEnd == -1 || elseEnd <= elseStart) return null
    val elseBranch = value.substring(elseStart, elseEnd)

    return ConditionalExpression(
        condition = condition.trim(),
        thenBranch = thenBranch,
        elseBranch = elseBranch,
    )
}

/**
 * Вычисляет булево условие из строкового выражения.
 *
 * @param rawExpression Строковое выражение с операторами ==, !=, >, <, >=, <=
 * @param contextManager Менеджер контекста для переменных
 * @return true или false
 */
private fun evalCondition(
    rawExpression: String,
    contextManager: IContextManager
): Boolean {
    val expression = resolveContextVariables(rawExpression.trim(), contextManager)

    val operators = listOf("==", "!=", ">=", "<=", ">", "<")
    val op = operators.firstOrNull { expression.contains(it) } ?: return false
    val parts = expression.split(op)
    if (parts.size != 2) return false

    val leftRaw = parts[0].trim()
    val rightRaw = parts[1].trim()

    val left = stripQuotes(leftRaw)
    val right = stripQuotes(rightRaw)

    val leftVal = evalArithmetic(left)
    val rightVal = evalArithmetic(right)

    return when {
        leftVal is Number && rightVal is Number -> {
            val l = leftVal.toDouble()
            val r = rightVal.toDouble()
            when (op) {
                "==" -> l == r
                "!=" -> l != r
                ">" -> l > r
                "<" -> l < r
                ">=" -> l >= r
                "<=" -> l <= r
                else -> false
            }
        }
        else -> {
            when (op) {
                "==" -> left == right
                "!=" -> left != right
                else -> false
            }
        }
    }
}

/**
 * Вычисляет арифметическое выражение (пока только остаток от деления).
 *
 * @param expr Строковое выражение
 * @return Результат: Long, Double или исходная строка
 */
private fun evalArithmetic(expr: String): Any {
    val trimmed = expr.trim()
    if (trimmed.contains("%")) {
        val parts = trimmed.split("%")
        if (parts.size == 2) {
            val left = parts[0].trim().toLongOrNull()
            val right = parts[1].trim().toLongOrNull()
            if (left != null && right != null && right != 0L) {
                return left % right
            }
        }
    }

    trimmed.toLongOrNull()?.let { return it }
    trimmed.toDoubleOrNull()?.let { return it }

    return trimmed
}

/**
 * Обрезает одинарные или двойные кавычки по краям строки, если они присутствуют.
 *
 * @param value Исходная строка
 * @return Строка без кавычек по краям
 */
private fun stripQuotes(value: String): String {
    if (value.length >= 2) {
        val first = value.first()
        val last = value.last()
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length - 1)
        }
    }
    return value
}

/**
 * Подставляет переменные контекста `@{microapp.var}` и `@@{var}` во **всех** вхождениях
 * (в том числе внутри условий `*if(...)*then*else*` и в ветках then/else).
 *
 * Порядок: сначала `@@{…}`, затем `@{…}` — иначе префикс `@{` из `@@{foo}` ошибочно
 * обрабатывался бы как microapp.
 *
 * @param raw Исходная строка
 * @param contextManager Менеджер контекста
 * @return Строка с подставленными переменными
 */
private fun resolveContextVariables(
    raw: String,
    contextManager: IContextManager
): String {
    if (raw.isEmpty()) return raw

    var result = ENGINE_CONTEXT_VAR_PATTERN.replace(raw) { match ->
        val name = match.groupValues[1].trim()
        if (name.isEmpty()) {
            match.value
        } else {
            contextManager.getEngineVariable(name)?.toString() ?: match.value
        }
    }

    result = MICROAPP_CONTEXT_VAR_PATTERN.replace(result) { match ->
        val content = match.groupValues[1]
        val parts = content.split(".", limit = 2)
        if (parts.size == 2) {
            val microappCode = parts[0].trim()
            val variableName = parts[1].trim()
            if (microappCode.isNotEmpty() && variableName.isNotEmpty()) {
                contextManager.getMicroappVariable(microappCode, variableName)?.toString() ?: match.value
            } else {
                match.value
            }
        } else {
            match.value
        }
    }

    return result
}
