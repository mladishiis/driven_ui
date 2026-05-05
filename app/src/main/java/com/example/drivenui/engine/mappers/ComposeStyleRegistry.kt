package com.example.drivenui.engine.mappers

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ColorStyle
import com.example.drivenui.engine.parser.models.ColorTheme
import com.example.drivenui.engine.parser.models.TextStyle

/**
 * Реестр стилей для Compose: текст и цвет по коду стиля.
 *
 * @property allStyles Все стили микроаппа
 */
class ComposeStyleRegistry(
    private val allStyles: AllStyles?
) {
    /**
     * Возвращает стиль текста по коду.
     *
     * @param code Код стиля
     * @return TextStyle или null если не найден
     */
    fun getTextStyle(code: String): TextStyle? =
        allStyles?.textStyles?.firstOrNull { it.code == code }

    /**
     * Возвращает стиль цвета по коду.
     *
     * @param code Код стиля
     * @return ColorStyle или null если не найден
     */
    fun getColorStyle(code: String): ColorStyle? =
        allStyles?.colorStyles?.firstOrNull { it.code == code }

    /**
     * Возвращает Compose-цвет по коду стиля с учётом opacity и ветки light/dark из манифеста стилей.
     *
     * Второй параметр должен отражать **системную** схему (светлая/тёмная), а не контраст «по фону родителя».
     *
     * @param code Код стиля цвета
     * @param isDarkTheme `true` — взять [ColorStyle.darkTheme], иначе [ColorStyle.lightTheme]
     * @return Compose Color или null если стиль не найден
     */
    fun getComposeColor(code: String, isDarkTheme: Boolean = false): Color? {
        val style = getColorStyle(code)
        if (style != null) {
            return (if (isDarkTheme) style.darkTheme else style.lightTheme).toComposeColor()
        }
        val hexIndex = code.indexOf('#')
        if (hexIndex >= 0) {
            return runCatching { Color(code.substring(hexIndex).toColorInt()) }.getOrNull()
        }
        return null
    }
}

/**
 * Превращает ColorTheme (HEX + opacity 0–100) в Compose Color с учётом прозрачности.
 *
 * @receiver Цветовая тема (светлая или тёмная)
 * @return Compose Color
 */
private fun ColorTheme.toComposeColor(): Color {
    val base = Color(color.toColorInt())
    val alpha = (opacity.coerceIn(0, 100)) / 100f
    return base.copy(alpha = alpha)
}