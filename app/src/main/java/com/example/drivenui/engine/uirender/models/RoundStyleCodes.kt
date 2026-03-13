package com.example.drivenui.engine.uirender.models

/**
 * Коды стилей скругления углов.
 *
 * @property code скругление всех углов (приоритет над top/bottom)
 * @property topCode скругление только верхних углов
 * @property bottomCode скругление только нижних углов
 */
data class RoundStyleCodes(
    val code: String? = null,
    val topCode: String? = null,
    val bottomCode: String? = null,
)
