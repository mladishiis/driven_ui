package com.example.drivenui.engine.value

import com.example.drivenui.engine.context.IContextManager

/**
 * Резолвит строковое значение с учётом условных выражений *if(...)*then(...)*else(...)
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
        elseBranch = elseBranch
    )
}

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

    // Удаляем обрамляющие кавычки, если они есть, чтобы
    // выражения вида value == "value" работали ожидаемо.
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
 * Пока только остаток от деления
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
 * Обрезаем одинарные или двойные кавычки по краям строки,
 * если они присутствуют.
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
 * Подставляет переменные контекста @{microapp.var} и @@{var},.
 */
private fun resolveContextVariables(
    raw: String,
    contextManager: IContextManager
): String {
    if (raw.startsWith("@{") && raw.endsWith("}")) {
        val content = raw.substring(2, raw.length - 1)
        val parts = content.split(".", limit = 2)
        if (parts.size == 2) {
            val microappCode = parts[0].trim()
            val variableName = parts[1].trim()
            if (microappCode.isNotEmpty() && variableName.isNotEmpty()) {
                val resolved = contextManager.getMicroappVariable(microappCode, variableName)
                return resolved?.toString() ?: raw
            }
        }
    }

    if (raw.startsWith("@@{") && raw.endsWith("}")) {
        val content = raw.substring(3, raw.length - 1).trim()
        if (content.isNotEmpty()) {
            val resolved = contextManager.getEngineVariable(content)
            return resolved?.toString() ?: raw
        }
    }

    return raw
}