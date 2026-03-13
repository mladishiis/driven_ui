package com.example.drivenui.engine.uirender.models

/**
 * Параметры цикла FOR в layout (verticalFor / horizontalFor).
 *
 * @property forIndexName имя переменной индекса для шаблона {#name}
 * @property maxForIndex максимальное значение индекса
 */
data class LayoutForParams(
    val forIndexName: String? = null,
    val maxForIndex: String? = null,
)
