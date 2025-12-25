package com.example.drivenui.engine.mappers

import com.example.drivenui.parser.models.AllStyles

/**
 * Простая реализация реестра стилей для Compose
 */
class ComposeStyleRegistry(
    private val allStyles: AllStyles?
) {
    fun getTextStyle(code: String): com.example.drivenui.parser.models.TextStyle? {
        return allStyles?.textStyles?.firstOrNull { it.code == code }
    }

    fun getColorStyle(code: String): com.example.drivenui.parser.models.ColorStyle? {
        return allStyles?.colorStyles?.firstOrNull { it.code == code }
    }

    fun getAlignmentStyle(code: String): com.example.drivenui.parser.models.AlignmentStyle? {
        return allStyles?.alignmentStyles?.firstOrNull { it.code == code }
    }

    fun getPaddingStyle(code: String): com.example.drivenui.parser.models.PaddingStyle? {
        return allStyles?.paddingStyles?.firstOrNull { it.code == code }
    }

    fun getRoundStyle(code: String): com.example.drivenui.parser.models.RoundStyle? {
        return allStyles?.roundStyles?.firstOrNull { it.code == code }
    }
}