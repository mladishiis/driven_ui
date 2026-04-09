package com.example.drivenui.engine.uirender.models

/**
 * Параметры цикла FOR в layout (verticalFor / horizontalFor).
 *
 * @property forIndexName имя переменной индекса для шаблона {#name}
 * @property maxForIndex шаблон максимума индекса (из XML; может содержать `${...}`)
 * @property resolvedMaxForIndex число итераций после `ForLayoutBinding` (строка для совместимости)
 */
data class LayoutForParams(
    val forIndexName: String? = null,
    val maxForIndex: String? = null,
    val resolvedMaxForIndex: String? = null,
)
