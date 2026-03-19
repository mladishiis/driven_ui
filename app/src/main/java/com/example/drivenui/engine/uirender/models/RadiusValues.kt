package com.example.drivenui.engine.uirender.models

/**
 * Значения радиуса скругления из properties (radius, radiusTop, radiusBottom).
 *
 * @property radius радиус всех углов в dp (приоритет над top/bottom)
 * @property radiusTop радиус верхних углов в dp
 * @property radiusBottom радиус нижних углов в dp
 */
data class RadiusValues(
    val radius: String? = null,
    val radiusTop: String? = null,
    val radiusBottom: String? = null,
)
