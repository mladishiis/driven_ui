package com.example.drivenui.parser

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.models.*
import com.example.drivenui.parser.parsers.*
import java.util.zip.ZipFile

/**
 * Главный парсер с поддержкой новой структуры компонентов
 */
class SDUIParserNew(private val context: Context) {

    private val styleParser = StyleParser()
    private val eventParser = EventParser()
    private val queryParser = QueryParser()
    private val microappParser = MicroappParser()
    private val widgetParser = WidgetParser()
    private val layoutParser = LayoutParser()
    private val componentParser = ComponentParser()

    /**
     * Результат парсинга с новой структурой
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
        val layouts: List<Layout> = emptyList()
    ) {
        /**
         * Проверяет, содержит ли результат какие-либо данные
         */
        fun hasData(): Boolean {
            return microapp != null ||
                    screens.isNotEmpty() ||
                    events?.events?.isNotEmpty() == true ||
                    eventActions?.eventActions?.isNotEmpty() == true ||
                    styles != null ||
                    queries.isNotEmpty() ||
                    screenQueries.isNotEmpty() ||
                    widgets.isNotEmpty() ||
                    layouts.isNotEmpty()
        }

        /**
         * Проверяет, есть ли в результате микроапп
         */
        fun hasMicroapp(): Boolean = microapp != null

        /**
         * Проверяет, есть ли в результате экраны
         */
        fun hasScreens(): Boolean = screens.isNotEmpty()

        /**
         * Проверяет, есть ли в результате стили
         */
        fun hasStyles(): Boolean = styles != null

        /**
         * Проверяет, есть ли в результате события
         */
        fun hasEvents(): Boolean = events?.events?.isNotEmpty() == true

        /**
         * Проверяет, есть ли в результате действия событий
         */
        fun hasEventActions(): Boolean = eventActions?.eventActions?.isNotEmpty() == true

        /**
         * Получает экран по коду
         */
        fun getScreenByCode(screenCode: String): ParsedScreen? {
            return screens.firstOrNull { it.screenCode == screenCode }
        }

        /**
         * Получает первый экран
         */
        fun getFirstScreen(): ParsedScreen? = screens.firstOrNull()

        /**
         * Получает текстовые стили
         */
        fun getTextStyles(): List<TextStyle> = styles?.textStyles ?: emptyList()

        /**
         * Получает цветовые стили
         */
        fun getColorStyles(): List<ColorStyle> = styles?.colorStyles ?: emptyList()

        /**
         * Получает скругления
         */
        fun getRoundStyles(): List<RoundStyle> = styles?.roundStyles ?: emptyList()

        /**
         * Получает отступы
         */
        fun getPaddingStyles(): List<PaddingStyle> = styles?.paddingStyles ?: emptyList()

        /**
         * Получает выравнивания
         */
        fun getAlignmentStyles(): List<AlignmentStyle> = styles?.alignmentStyles ?: emptyList()

        /**
         * Подсчитывает общее количество компонентов во всех экранах
         */
        fun countAllComponents(): Int {
            var total = 0
            screens.forEach { screen ->
                screen.rootComponent?.let { root ->
                    total += countComponentsRecursive(root)
                }
            }
            return total
        }

        /**
         * Получает статистику парсинга
         */
        fun getStats(): Map<String, Any> {
            return mapOf(
                "microapp" to (microapp?.title ?: "нет"),
                "screens" to screens.size,
                "textStyles" to (styles?.textStyles?.size ?: 0),
                "colorStyles" to (styles?.colorStyles?.size ?: 0),
                "roundStyles" to (styles?.roundStyles?.size ?: 0),
                "paddingStyles" to (styles?.paddingStyles?.size ?: 0),
                "alignmentStyles" to (styles?.alignmentStyles?.size ?: 0),
                "queries" to queries.size,
                "screenQueries" to screenQueries.size,
                "events" to (events?.events?.size ?: 0),
                "eventActions" to (eventActions?.eventActions?.size ?: 0),
                "widgets" to widgets.size,
                "layouts" to layouts.size,
                "componentsCount" to countAllComponents(),
                "hasComponentStructure" to screens.any { it.rootComponent != null }
            )
        }

        /**
         * Логирует краткую информацию о результате
         */
        fun logSummary() {
            Log.d("ParsedMicroappResult", "=== Краткая информация о результате ===")
            Log.d("ParsedMicroappResult", "Микроапп: ${microapp?.title ?: "нет"}")
            Log.d("ParsedMicroappResult", "Экраны: ${screens.size}")
            screens.forEachIndexed { index, screen ->
                Log.d("ParsedMicroappResult", "  $index: ${screen.title} (${screen.screenCode})")
                if (screen.rootComponent != null) {
                    Log.d("ParsedMicroappResult", "    Корневой компонент: ${screen.rootComponent.title}")
                    Log.d("ParsedMicroappResult", "    Компонентов: ${countComponentsRecursive(screen.rootComponent)}")
                }
            }
            Log.d("ParsedMicroappResult", "Стили текста: ${getTextStyles().size}")
            Log.d("ParsedMicroappResult", "Стили цвета: ${getColorStyles().size}")
            Log.d("ParsedMicroappResult", "События: ${events?.events?.size ?: 0}")
            Log.d("ParsedMicroappResult", "Действия событий: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("ParsedMicroappResult", "Запросы: ${queries.size}")
            Log.d("ParsedMicroappResult", "Виджеты: ${widgets.size}")
            Log.d("ParsedMicroappResult", "Лэйауты: ${layouts.size}")
            Log.d("ParsedMicroappResult", "Всего компонентов: ${countAllComponents()}")
            Log.d("ParsedMicroappResult", "=======================================")
        }

        /**
         * Рекурсивно подсчитывает компоненты
         */
        private fun countComponentsRecursive(component: Component): Int {
            var count = 1 // текущий компонент
            component.children.forEach { child ->
                count += countComponentsRecursive(child)
            }
            return count
        }
    }

    /**
     * Загружает и парсит микроапп из файла в assets с новой структурой
     */
    fun parseFromAssetsNew(fileName: String): ParsedMicroappResult {
        return try {
            Log.d("SDUIParserNew", "Начинаем парсинг файла с новой структурой: $fileName")
            val inputStream = context.assets.open(fileName)
            val xmlContent = inputStream.bufferedReader().use { it.readText() }
            parseFullXmlContentNew(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при загрузке файла из assets: $fileName", e)
            ParsedMicroappResult()
        }
    }

    /**
     * Парсит полный XML микроаппа с новой структурой компонентов
     */
    private fun parseFullXmlContentNew(xmlContent: String): ParsedMicroappResult {
        return try {
            Log.d("SDUIParserNew", "Начинаем парсинг полного XML с новой структурой")

            val microapp = parseMicroappFromFullXml(xmlContent)
            val styles = parseStylesFromFullXml(xmlContent)
            val events = parseEventsFromFullXml(xmlContent)
            val eventActions = parseEventActionsFromFullXml(xmlContent)
            val screens = componentParser.parseAllComponentsFromFullXml(xmlContent)
            val queries = parseQueriesFromFullXml(xmlContent)
            val screenQueries = parseScreenQueriesFromFullXml(xmlContent)
            val widgets = parseWidgetsFromFullXml(xmlContent)
            val layouts = parseLayoutsFromFullXml(xmlContent)

            Log.d("SDUIParserNew", "Парсинг завершен:")
            Log.d("SDUIParserNew", "  - microapp: ${microapp?.title ?: "не найден"}")
            Log.d("SDUIParserNew", "  - styles: ${styles != null}")
            Log.d("SDUIParserNew", "  - events: ${events?.events?.size ?: 0}")
            Log.d("SDUIParserNew", "  - eventActions: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("SDUIParserNew", "  - screens: ${screens.size}")
            Log.d("SDUIParserNew", "  - queries: ${queries.size}")
            Log.d("SDUIParserNew", "  - screenQueries: ${screenQueries.size}")
            Log.d("SDUIParserNew", "  - widgets: ${widgets.size}")
            Log.d("SDUIParserNew", "  - layouts: ${layouts.size}")

            // Логируем структуру компонентов
            screens.forEachIndexed { index, screen ->
                Log.d("SDUIParserNew", "Экран $index: ${screen.title}")
                screen.rootComponent?.let {
                    Log.d("SDUIParserNew", "  Корневой компонент: ${it.title} с ${it.children.size} детьми")
                }
            }

            // Создаем результат
            val result = ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                events = events,
                eventActions = eventActions,
                screens = screens,
                queries = queries,
                screenQueries = screenQueries,
                widgets = widgets,
                layouts = layouts
            )

            // Логируем итоговую статистику
            result.logSummary()

            result
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при парсинге полного XML", e)
            ParsedMicroappResult()
        }
    }

    // Существующие методы извлечения блоков
    private fun parseMicroappFromFullXml(xmlContent: String): Microapp? {
        return extractAndParseBlock(xmlContent, "microapp") { blockContent ->
            microappParser.parseMicroapp(blockContent)
        }
    }

    private fun parseStylesFromFullXml(xmlContent: String): AllStyles? {
        return extractAndParseBlock(xmlContent, "allStyles") { blockContent ->
            styleParser.parseStyles(blockContent)
        }
    }

    private fun parseEventsFromFullXml(xmlContent: String): AllEvents? {
        return extractAndParseBlock(xmlContent, "allEvents") { blockContent ->
            eventParser.parseEvents(blockContent)
        }
    }

    private fun parseEventActionsFromFullXml(xmlContent: String): AllEventActions? {
        return extractAndParseBlock(xmlContent, "allEventActions") { blockContent ->
            eventParser.parseEventActions(blockContent)
        }
    }

    private fun parseQueriesFromFullXml(xmlContent: String): List<Query> {
        return extractAndParseBlock(xmlContent, "allQueries") { blockContent ->
            queryParser.parseQueries(blockContent)
        } ?: emptyList()
    }

    private fun parseScreenQueriesFromFullXml(xmlContent: String): List<ScreenQuery> {
        return extractAndParseBlock(xmlContent, "screenQueries") { blockContent ->
            queryParser.parseScreenQueries(blockContent)
        } ?: emptyList()
    }

    private fun parseWidgetsFromFullXml(xmlContent: String): List<Widget> {
        return extractAndParseBlock(xmlContent, "allWidgets") { blockContent ->
            widgetParser.parseWidgets(blockContent)
        } ?: emptyList()
    }

    private fun parseLayoutsFromFullXml(xmlContent: String): List<Layout> {
        return extractAndParseBlock(xmlContent, "allLayouts") { blockContent ->
            layoutParser.parseLayouts(blockContent)
        } ?: emptyList()
    }

    private inline fun <T> extractAndParseBlock(
        xmlContent: String,
        blockName: String,
        parser: (String) -> T
    ): T? {
        val startTag = "<$blockName>"
        val endTag = "</$blockName>"

        val startIndex = xmlContent.indexOf(startTag)
        if (startIndex == -1) {
            Log.d("SDUIParserNew", "Блок <$blockName> не найден в XML")
            return null
        }

        val endIndex = xmlContent.indexOf(endTag, startIndex)
        if (endIndex == -1) {
            Log.w("SDUIParserNew", "Не найден закрывающий тег для блока <$blockName>")
            return null
        }

        val blockContent = xmlContent.substring(startIndex, endIndex + endTag.length)
        Log.d("SDUIParserNew", "Извлечен блок <$blockName>, размер: ${blockContent.length} символов")

        return try {
            parser(blockContent)
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при парсинге блока <$blockName>", e)
            null
        }
    }
}