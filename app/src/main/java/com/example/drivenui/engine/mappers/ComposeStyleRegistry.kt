package com.example.drivenui.engine.mappers

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ColorStyle
import com.example.drivenui.parser.models.ColorTheme
import com.example.drivenui.parser.models.RoundStyle
import com.example.drivenui.parser.models.TextStyle

/**
 * Простая реализация реестра стилей для Compose
 */
class ComposeStyleRegistry(
    private val allStyles: AllStyles?
) {
    fun getTextStyle(code: String): TextStyle? =
        allStyles?.textStyles?.firstOrNull { it.code == code }

    fun getColorStyle(code: String): ColorStyle? =
        allStyles?.colorStyles?.firstOrNull { it.code == code }

    fun getRoundStyle(code: String): RoundStyle? =
        allStyles?.roundStyles?.firstOrNull { it.code == code }

    /**
     * Готовый Compose-цвет по коду стиля с учётом opacity и темы (пока всегда lightTheme).
     */
    fun getComposeColor(code: String): Color? =
        getColorStyle(code)?.lightTheme?.toComposeColor()
}

/**
 * Превращает ColorTheme (HEX + opacity 0–100) в Compose Color с учётом прозрачности.
 */
private fun ColorTheme.toComposeColor(): Color {
    val base = Color(color.toColorInt())
    val alpha = (opacity.coerceIn(0, 100)) / 100f
    return base.copy(alpha = alpha)
}