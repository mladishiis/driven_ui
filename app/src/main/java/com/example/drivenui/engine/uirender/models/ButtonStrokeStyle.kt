package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.graphics.Color

/**
 * Обводка кнопки: сырьё из XML и значения, готовые для отрисовки после `resolveScreen`.
 *
 * @property width толщина в dp из разметки; допускает шаблоны `${}` и `{#}`
 * @property colorStyleCode код цвета из слота стиля strokeColorStyle
 * @property resolvedWidthDp толщина в dp после резолва; null, если обводку не рисуют
 * @property resolvedColor цвет после резолва для `Modifier.border`
 */
data class ButtonStrokeStyle(
    val width: String? = null,
    val colorStyleCode: String? = null,
    val resolvedWidthDp: Int? = null,
    val resolvedColor: Color? = null,
)
