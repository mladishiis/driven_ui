package com.example.drivenui.engine.parser

import android.util.Log
import com.example.drivenui.engine.parser.models.*
import com.example.drivenui.engine.parser.parsers.ComponentParser
import com.example.drivenui.engine.parser.parsers.MicroappParser
import com.example.drivenui.engine.parser.parsers.QueryParser
import com.example.drivenui.engine.parser.parsers.StyleParser

/**
 * Главный парсер SDUI. Работает ТОЛЬКО со строками XML.
 */
class SDUIParser {

    private val styleParser = StyleParser()
    private val queryParser = QueryParser()
    private val microappParser = MicroappParser()
    private val componentParser = ComponentParser()

    /**
     * Результат парсинга
     */
    data class ParsedMicroappResult(
        val microapp: Microapp? = null,
        val styles: AllStyles? = null,
        val events: AllEvents? = null,
        val eventActions: AllEventActions? = null,
        val screens: List<ParsedScreen> = emptyList(),
        val queries: List<Query> = emptyList(),
        val screenQueries: List<ScreenQuery> = emptyList(),
        val widgets: List<Widget> = emptyList(),
        val layouts: List<Layout> = emptyList(),
        val dataContext: DataContext? = null,
    ) {

        /**
         * Проверяет, содержит ли результат какие-либо данные
         */
        fun hasData(): Boolean =
            microapp != null ||
                    screens.isNotEmpty() ||
                    styles != null ||
                    queries.isNotEmpty() ||
                    screenQueries.isNotEmpty() ||
                    widgets.isNotEmpty() ||
                    layouts.isNotEmpty()

        fun getScreenByCode(screenCode: String): ParsedScreen? =
            screens.firstOrNull { it.screenCode == screenCode }

        fun getTextStyles(): List<TextStyle> =
            styles?.textStyles.orEmpty()

        fun getColorStyles(): List<ColorStyle> =
            styles?.colorStyles.orEmpty()

        fun countAllComponents(): Int =
            screens.sumOf { screen ->
                screen.rootComponent?.let { countComponentsRecursive(it) } ?: 0
            }

        fun getStats(): Map<String, Any> = mapOf(
            "microapp" to (microapp?.title ?: "нет"),
            "screens" to screens.size,
            "textStyles" to (styles?.textStyles?.size ?: 0),
            "colorStyles" to (styles?.colorStyles?.size ?: 0),
            "roundStyles" to (styles?.roundStyles?.size ?: 0),
            "paddingStyles" to (styles?.paddingStyles?.size ?: 0),
            "alignmentStyles" to (styles?.alignmentStyles?.size ?: 0),
            "queries" to queries.size,
            "screenQueries" to screenQueries.size,
            "widgets" to widgets.size,
            "layouts" to layouts.size,
            "componentsCount" to countAllComponents(),
        )

        fun logSummary() {
            Log.d("SDUIParser", "====== SDUI PARSE RESULT ======")
            Log.d("SDUIParser", "Microapp: ${microapp?.title ?: "нет"}")
            Log.d("SDUIParser", "Screens: ${screens.size}")

            screens.forEach { screen ->
                Log.d(
                    "SDUIParser",
                    "Screen ${screen.screenCode}: ${screen.title}, components=${screen.rootComponent?.let {
                        countComponentsRecursive(it)
                    } ?: 0}, requests=${screen.requests.size}"
                )
            }

            Log.d("SDUIParser", "Text styles: ${getTextStyles().size}")
            Log.d("SDUIParser", "Color styles: ${getColorStyles().size}")
            Log.d("SDUIParser", "Queries: ${queries.size}")
            Log.d("SDUIParser", "ScreenQueries: ${screenQueries.size}")
            Log.d("SDUIParser", "Total components: ${countAllComponents()}")
            Log.d("SDUIParser", "==============================")
        }

        fun getResolvedValues(): Map<String, String> {
            val values = mutableMapOf<String, String>()
            screens.forEach { screen ->
                screen.rootComponent?.let { collectResolvedValues(it, values) }
            }
            return values
        }

        private fun collectResolvedValues(component: Component, values: MutableMap<String, String>) {
            // Пример: собираем только те свойства, у которых rawValue != resolvedValue
            component.properties.forEach { prop ->
                prop.resolvedValue?.let { values[prop.code] = it }
            }
            component.children.forEach { child ->
                collectResolvedValues(child, values)
            }
        }

        private fun countComponentsRecursive(component: Component): Int =
            1 + component.children.sumOf { countComponentsRecursive(it) }
    }

    /**
     * ЕДИНСТВЕННЫЙ публичный метод парсинга
     */
    fun parse(
        microappXml: String,
        stylesXml: String,
        queriesXml: String,
        screens: List<Pair<String, String>> // fileName → xml
    ): ParsedMicroappResult {

        return try {
            Log.d("SDUIParser", "=== Start SDUI parsing ===")

            val microapp = parseMicroapp(microappXml)
            val styles = parseStyles(stylesXml)
            val queries = parseQueries(queriesXml)

            val parsedScreens = parseScreens(screens)
            val screenQueries = parseScreenQueries(screens)

            val queriesByScreen = screenQueries.groupBy { it.screenCode }

            val screensWithQueries = parsedScreens.map { screen ->
                screen.copy(
                    requests = queriesByScreen[screen.screenCode].orEmpty(),
                )
            }

            ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                screens = screensWithQueries,
                queries = queries,
                screenQueries = screenQueries,
            ).also { it.logSummary() }

        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга SDUI", e)
            ParsedMicroappResult()
        }
    }

    private fun parseScreens(
        screens: List<Pair<String, String>>
    ): List<ParsedScreen> =
        screens.mapNotNull { (name, xml) ->
            try {
                componentParser.parseSingleScreenXml(xml)?.also {
                    Log.d("SDUIParser", "Screen parsed: ${it.screenCode} ($name)")
                }
            } catch (e: Exception) {
                Log.e("SDUIParser", "Ошибка парсинга экрана $name", e)
                null
            }
        }

    private fun parseScreenQueries(
        screens: List<Pair<String, String>>
    ): List<ScreenQuery> =
        screens.flatMap { (name, xml) ->
            try {
                queryParser.parseScreenQueries(xml)
            } catch (e: Exception) {
                Log.e("SDUIParser", "Ошибка парсинга screenQueries в $name", e)
                emptyList()
            }
        }

    private fun parseQueries(xml: String): List<Query> =
        try {
            queryParser.parseAllQueries(xml)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга queries", e)
            emptyList()
        }

    private fun parseMicroapp(xml: String): Microapp? =
        try {
            microappParser.parseMicroapp(xml)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга microapp", e)
            null
        }

    private fun parseStyles(xml: String): AllStyles? =
        try {
            styleParser.parseStyles(xml)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга styles", e)
            null
        }
}