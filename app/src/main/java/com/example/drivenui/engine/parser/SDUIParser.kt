package com.example.drivenui.engine.parser

import android.util.Log
import com.example.drivenui.engine.parser.models.AllEventActions
import com.example.drivenui.engine.parser.models.AllEvents
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ColorStyle
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.parser.models.Layout
import com.example.drivenui.engine.parser.models.Microapp
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.parser.models.TextStyle
import com.example.drivenui.engine.parser.models.Widget
import com.example.drivenui.engine.parser.parsers.ComponentParser
import com.example.drivenui.engine.parser.parsers.MicroappParser
import com.example.drivenui.engine.parser.parsers.StyleParser

/**
 * Главный парсер SDUI-конфигурации микроаппов.
 *
 * Принимает сырые XML-строки (microapp, styles, screens) и возвращает
 * структурированный [ParsedMicroappResult]. Не работает с файлами напрямую.
*/
class SDUIParser {

    private val styleParser = StyleParser()
    private val microappParser = MicroappParser()
    private val componentParser = ComponentParser()

    /**
     * Результат парсинга микроаппа. Содержит распарсенные экраны, стили и метаданные.
     *
     * @property microapp метаданные микроаппа (deeplink, code, persistents)
     * @property styles текстовые, цветовые и прочие стили
     * @property events события микроаппа
     * @property eventActions действия событий
     * @property screens список экранов с деревом компонентов
     * @property widgets реестр виджетов
     * @property layouts реестр лейаутов
     * @property dataContext контекст данных для биндингов
     */
    data class ParsedMicroappResult(
        val microapp: Microapp? = null,
        val styles: AllStyles? = null,
        val events: AllEvents? = null,
        val eventActions: AllEventActions? = null,
        val screens: List<ParsedScreen> = emptyList(),
        val widgets: List<Widget> = emptyList(),
        val layouts: List<Layout> = emptyList(),
        val dataContext: DataContext? = null,
    ) {

        /**
         * Проверяет, содержит ли результат какие-либо данные.
         *
         * @return true, если есть микроапп, экраны, стили или иные данные
         */
        fun hasData(): Boolean =
            microapp != null ||
                    screens.isNotEmpty() ||
                    styles != null ||
                    widgets.isNotEmpty() ||
                    layouts.isNotEmpty()

        /**
         * Возвращает экран по коду.
         *
         * @param screenCode код экрана
         * @return экран или null
         */
        fun getScreenByCode(screenCode: String): ParsedScreen? =
            screens.firstOrNull { it.screenCode == screenCode }

        /**
         * Возвращает список текстовых стилей.
         *
         * @return список [TextStyle]
         */
        fun getTextStyles(): List<TextStyle> =
            styles?.textStyles.orEmpty()

        /**
         * Возвращает список цветовых стилей.
         *
         * @return список [ColorStyle]
         */
        fun getColorStyles(): List<ColorStyle> =
            styles?.colorStyles.orEmpty()

        /**
         * Подсчитывает общее количество компонентов во всех экранах.
         *
         * @return число компонентов
         */
        fun countAllComponents(): Int =
            screens.sumOf { screen ->
                screen.rootComponent?.let { countComponentsRecursive(it) } ?: 0
            }

        /**
         * Возвращает статистику парсинга.
         *
         * @return карта с ключами microapp, screens, textStyles, colorStyles и т.д.
         */
        fun getStats(): Map<String, Any> = mapOf(
            "microapp" to (microapp?.title ?: "нет"),
            "screens" to screens.size,
            "textStyles" to (styles?.textStyles?.size ?: 0),
            "colorStyles" to (styles?.colorStyles?.size ?: 0),
            "roundStyles" to (styles?.roundStyles?.size ?: 0),
            "paddingStyles" to (styles?.paddingStyles?.size ?: 0),
            "alignmentStyles" to (styles?.alignmentStyles?.size ?: 0),
            "widgets" to widgets.size,
            "layouts" to layouts.size,
            "componentsCount" to countAllComponents(),
        )

        /**
         * Собирает разрешённые значения биндингов из компонентов.
         *
         * @return карта «код свойства» → «разрешённое значение»
         */
        fun getResolvedValues(): Map<String, String> {
            val values = mutableMapOf<String, String>()
            screens.forEach { screen ->
                screen.rootComponent?.let { collectResolvedValues(it, values) }
            }
            return values
        }

        private fun collectResolvedValues(component: Component, values: MutableMap<String, String>) {
            values.putAll(component.properties)
            component.children.forEach { child ->
                collectResolvedValues(child, values)
            }
        }

        private fun countComponentsRecursive(component: Component): Int =
            1 + component.children.sumOf { countComponentsRecursive(it) }
    }

    /**
     * Выполняет полный парсинг конфигурации микроаппа.
     *
     * @param microappXml XML метаданных микроаппа
     * @param stylesXml XML стилей (текст, цвета, отступы)
     * @param screens список пар (имя файла, xml-содержимое экрана)
     * @return результат парсинга или пустой [ParsedMicroappResult] при ошибке
     */
    fun parse(
        microappXml: String,
        stylesXml: String,
        screens: List<Pair<String, String>>,
    ): ParsedMicroappResult {

        return try {
            val microapp = parseMicroapp(microappXml)
            val styles = parseStyles(stylesXml)
            val parsedScreens = parseScreens(screens)

            ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                screens = parsedScreens,
            )

        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка разбора SDUI", e)
            ParsedMicroappResult()
        }
    }

    private fun parseScreens(
        screens: List<Pair<String, String>>,
    ): List<ParsedScreen> =
        screens.mapNotNull { (name, xml) ->
            try {
                componentParser.parseSingleScreenXml(xml)
            } catch (e: Exception) {
                Log.e("SDUIParser", "Ошибка парсинга экрана $name", e)
                null
            }
        }

    private fun parseMicroapp(xml: String): Microapp? =
        try {
            microappParser.parseMicroapp(xml)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга микроаппа", e)
            null
        }

    private fun parseStyles(xml: String): AllStyles? =
        try {
            styleParser.parseStyles(xml)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга стилей", e)
            null
        }
}