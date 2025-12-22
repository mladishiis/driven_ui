package com.example.drivenui.parser.models

/**
 * Стиль текста
 *
 * @property code Уникальный код стиля текста (например, "headlineM", "bodyS.tight.normal")
 * @property fontFamily Семейство шрифтов (headline/body/display)
 * @property fontSize Размер шрифта в sp
 * @property fontWeight Толщина шрифта (400 - normal, 500 - medium, 700 - bold)
 */
data class TextStyle(
    val code: String,
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: Int
)

/**
 * Цветовая тема для светлой или темной темы
 *
 * @property color Цвет в формате HEX (например, "#1D72FF")
 * @property opacity Прозрачность в процентах (0-100, по умолчанию 100)
 */
data class ColorTheme(
    val color: String,
    val opacity: Int = 100
)

/**
 * Стиль цвета с поддержкой светлой и темной темы
 *
 * @property code Уникальный код стиля цвета (например, "semantic/text/primary")
 * @property lightTheme Цвет для светлой темы
 * @property darkTheme Цвет для темной темы
 */
data class ColorStyle(
    val code: String,
    val lightTheme: ColorTheme,
    val darkTheme: ColorTheme
)

/**
 * Стиль выравнивания
 *
 * @property code Код выравнивания (например, "AlignLeft", "AlignCenter")
 */
data class AlignmentStyle(
    val code: String
)

/**
 * Стиль отступов
 *
 * @property code Уникальный код отступа в формате "padding[left]-[top]-[right]-[bottom]"
 * @property paddingLeft Отступ слева в пикселях
 * @property paddingTop Отступ сверху в пикселях
 * @property paddingRight Отступ справа в пикселях
 * @property paddingBottom Отступ снизу в пикселях
 */
data class PaddingStyle(
    val code: String,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int
)

/**
 * Стиль скругления углов
 *
 * @property code Уникальный код скругления в формате "radius[значение]"
 * @property radiusValue Значение радиуса скругления в пикселях
 */
data class RoundStyle(
    val code: String,
    val radiusValue: Int
)

/**
 * Контейнер всех стилей микроаппа
 *
 * @property textStyles Список стилей текста
 * @property colorStyles Список стилей цвета
 * @property alignmentStyles Список стилей выравнивания
 * @property paddingStyles Список стилей отступов
 * @property roundStyles Список стилей скругления
 */
data class AllStyles(
    val textStyles: List<TextStyle>,
    val colorStyles: List<ColorStyle>,
    val alignmentStyles: List<AlignmentStyle>,
    val paddingStyles: List<PaddingStyle>,
    val roundStyles: List<RoundStyle>
)