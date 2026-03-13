package com.example.drivenui.engine.uirender.models

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Резолвенные значения радиуса скругления углов (в dp).
 *
 * [all] — скругление всех углов (приоритет над top/bottom).
 * [top] — скругление только верхних углов.
 * [bottom] — скругление только нижних углов.
 */
data class CornerRadius(
    val all: Int? = null,
    val top: Int? = null,
    val bottom: Int? = null,
) {
    /** Создаёт RoundedCornerShape или null, если ни одно значение не задано. */
    fun toRoundedCornerShape(): RoundedCornerShape? = when {
        all != null -> RoundedCornerShape(all.dp)
        top != null || bottom != null -> RoundedCornerShape(
            topStart = (top ?: 0).dp,
            topEnd = (top ?: 0).dp,
            bottomEnd = (bottom ?: 0).dp,
            bottomStart = (bottom ?: 0).dp,
        )
        else -> null
    }
}
