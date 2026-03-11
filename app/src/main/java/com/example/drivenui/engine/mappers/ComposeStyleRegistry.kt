package com.example.drivenui.engine.mappers

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ColorStyle
import com.example.drivenui.engine.parser.models.ColorTheme
import com.example.drivenui.engine.parser.models.RoundStyle
import com.example.drivenui.engine.parser.models.TextStyle

/**
 * Реестр стилей для Compose, предоставляет доступ к TextStyle, ColorStyle и RoundStyle по коду.
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
     * Возвращает стиль скругления по коду.
     *
     * @param code Код стиля
     * @return RoundStyle или null если не найден
     */
    fun getRoundStyle(code: String): RoundStyle? =
        allStyles?.roundStyles?.firstOrNull { it.code == code }

    /**
     * Возвращает Compose-цвет по коду стиля с учётом opacity и темы (пока всегда lightTheme).
     *
     * @param code Код стиля цвета
     * @return Compose Color или null если стиль не найден
     */
    fun getComposeColor(code: String): Color? =
        getColorStyle(code)?.lightTheme?.toComposeColor()
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