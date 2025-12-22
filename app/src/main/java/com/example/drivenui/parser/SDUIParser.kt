package com.example.drivenui.parser

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.models.*
import com.example.drivenui.parser.parsers.*
import java.util.zip.ZipFile

/**
 * Главный парсер Driven UI микроаппов
 *
 * Поддерживает загрузку микроаппов из:
 * - XML файлов в assets
 * - ZIP архивов с раздельными XML файлами
 * - Единого XML файла со всей структурой
 */
class SDUIParser(private val context: Context) {

    private val styleParser = StyleParser()
    private val eventParser = EventParser()
    private val screenParser = ScreenParser()
    private val queryParser = QueryParser()
    private val microappParser = MicroappParser()
    private val widgetParser = WidgetParser()
    private val layoutParser = LayoutParser()

    data class ParsedMicroapp(
        val microapp: Microapp? = null,
        val styles: AllStyles? = null,
        val events: AllEvents? = null,
        val eventActions: AllEventActions? = null,
        val screens: List<Screen> = emptyList(),
        val queries: List<Query> = emptyList(),
        val screenQueries: List<ScreenQuery> = emptyList(),
        val widgets: List<Widget> = emptyList(),
        val layouts: List<Layout> = emptyList()
    ) {
        /**
         * Проверяет, содержит ли результат парсинга какие-либо данные
         *
         * @return true если есть хотя бы какие-то данные (микроапп, экраны, стили и т.д.)
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
         *
         * @param screenCode Код экрана
         * @return Экран или null если не найден
         */
        fun getScreenByCode(screenCode: String): Screen? {
            return screens.firstOrNull { it.screenCode == screenCode }
        }

        /**
         * Получает первый экран
         *
         * @return Первый экран или null если экранов нет
         */
        fun getFirstScreen(): Screen? = screens.firstOrNull()

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
         * Логирует краткую информацию о результате
         */
        fun logSummary() {
            Log.d("ParsedMicroapp", "=== Краткая информация о результате ===")
            Log.d("ParsedMicroapp", "Микроапп: ${microapp?.title ?: "нет"}")
            Log.d("ParsedMicroapp", "Экраны: ${screens.size}")
            Log.d("ParsedMicroapp", "Стили текста: ${getTextStyles().size}")
            Log.d("ParsedMicroapp", "Стили цвета: ${getColorStyles().size}")
            Log.d("ParsedMicroapp", "События: ${events?.events?.size ?: 0}")
            Log.d("ParsedMicroapp", "Действия событий: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("ParsedMicroapp", "Запросы: ${queries.size}")
            Log.d("ParsedMicroapp", "Виджеты: ${widgets.size}")
            Log.d("ParsedMicroapp", "Лэйауты: ${layouts.size}")
            Log.d("ParsedMicroapp", "=======================================")
        }

        /**
         * Создает текстовое описание результата
         */
        fun toDescription(): String {
            return buildString {
                append("Результат парсинга:\n")
                microapp?.let {
                    append("• Микроапп: ${it.title} (${it.code})\n")
                }
                append("• Экран${if (screens.size != 1) "ов" else ""}: ${screens.size}\n")
                append("• Стили текста: ${getTextStyles().size}\n")
                append("• Стили цвета: ${getColorStyles().size}\n")
                append("• События: ${events?.events?.size ?: 0}\n")
                append("• Действия событий: ${eventActions?.eventActions?.size ?: 0}\n")
                append("• Запросы API: ${queries.size}\n")
                append("• Виджеты: ${widgets.size}\n")
                append("• Лэйауты: ${layouts.size}")
            }
        }
    }

    /**
     * Загружает и парсит микроапп из файла в assets
     *
     * @param fileName Имя файла в папке assets (например, "microapp.xml")
     * @return [ParsedMicroapp] результат парсинга
     */
    fun parseFromAssets(fileName: String): ParsedMicroapp {
        return try {
            Log.d("SDUIParser", "Начинаем парсинг файла из assets: $fileName")
            val inputStream = context.assets.open(fileName)
            val xmlContent = inputStream.bufferedReader().use { it.readText() }
            Log.d("SDUIParser", "Файл загружен, размер: ${xmlContent.length} символов")
            parseFullXmlContent(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при загрузке файла из assets: $fileName", e)
            ParsedMicroapp()
        }
    }

    /**
     * Загружает и парсит микроапп из ZIP архива
     *
     * @param zipFilePath Путь к ZIP архиву
     * @return [ParsedMicroapp] результат парсинга
     *
     * @note Ожидает структуру архива с файлами: styles.xml, events.xml, screens.xml,
     *       queries.xml, microapp.xml, widgets.xml, layouts.xml
     */
    fun parseFromZip(zipFilePath: String): ParsedMicroapp {
        return try {
            val zipFile = ZipFile(zipFilePath)
            var parsedMicroapp = ParsedMicroapp()

            zipFile.entries().asSequence().forEach { entry ->
                if (!entry.isDirectory) {
                    val content = zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
                    when (entry.name) {
                        "microapp.xml" -> {
                            val microapp = microappParser.parseMicroapp(content)
                            parsedMicroapp = parsedMicroapp.copy(microapp = microapp)
                        }
                        "styles.xml" -> {
                            val styles = styleParser.parseStyles(content)
                            parsedMicroapp = parsedMicroapp.copy(styles = styles)
                        }
                        "events.xml" -> {
                            val events = eventParser.parseEvents(content)
                            parsedMicroapp = parsedMicroapp.copy(events = events)
                        }
                        "eventActions.xml" -> {
                            val eventActions = eventParser.parseEventActions(content)
                            parsedMicroapp = parsedMicroapp.copy(eventActions = eventActions)
                        }
                        "screens.xml" -> {
                            val screens = screenParser.parseScreens(content)
                            parsedMicroapp = parsedMicroapp.copy(screens = screens)
                        }
                        "queries.xml" -> {
                            val queries = queryParser.parseQueries(content)
                            parsedMicroapp = parsedMicroapp.copy(queries = queries)
                        }
                        "screenQueries.xml" -> {
                            val screenQueries = queryParser.parseScreenQueries(content)
                            parsedMicroapp = parsedMicroapp.copy(screenQueries = screenQueries)
                        }
                        "widgets.xml" -> {
                            val widgets = widgetParser.parseWidgets(content)
                            parsedMicroapp = parsedMicroapp.copy(widgets = widgets)
                        }
                        "layouts.xml" -> {
                            val layouts = layoutParser.parseLayouts(content)
                            parsedMicroapp = parsedMicroapp.copy(layouts = layouts)
                        }
                    }
                }
            }

            zipFile.close()
            parsedMicroapp
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге ZIP архива: $zipFilePath", e)
            ParsedMicroapp()
        }
    }

    /**
     * Парсит полный XML микроаппа из единого файла
     *
     * @param xmlContent XML содержимое микроаппа
     * @return [ParsedMicroapp] результат парсинга
     */
    private fun parseFullXmlContent(xmlContent: String): ParsedMicroapp {
        return try {
            Log.d("SDUIParser", "Начинаем парсинг полного XML")

            val microapp = parseMicroappFromFullXml(xmlContent)
            val styles = parseStylesFromFullXml(xmlContent)
            val events = parseEventsFromFullXml(xmlContent)
            val eventActions = parseEventActionsFromFullXml(xmlContent)
            val screens = parseScreensFromFullXml(xmlContent)
            val queries = parseQueriesFromFullXml(xmlContent)
            val screenQueries = parseScreenQueriesFromFullXml(xmlContent)
            val widgets = parseWidgetsFromFullXml(xmlContent)
            val layouts = parseLayoutsFromFullXml(xmlContent)

            Log.d("SDUIParser", "Парсинг завершен:")
            Log.d("SDUIParser", "  - microapp: ${microapp?.title ?: "не найден"}")
            Log.d("SDUIParser", "  - styles: ${styles != null}")
            Log.d("SDUIParser", "  - events: ${events?.events?.size ?: 0}")
            Log.d("SDUIParser", "  - eventActions: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("SDUIParser", "  - screens: ${screens.size}")
            Log.d("SDUIParser", "  - queries: ${queries.size}")
            Log.d("SDUIParser", "  - screenQueries: ${screenQueries.size}")
            Log.d("SDUIParser", "  - widgets: ${widgets.size}")
            Log.d("SDUIParser", "  - layouts: ${layouts.size}")

            ParsedMicroapp(
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
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге полного XML", e)
            ParsedMicroapp()
        }
    }

    /**
     * Извлекает и парсит модель микроаппа из полного XML
     */
    private fun parseMicroappFromFullXml(xmlContent: String): Microapp? {
        return try {
            extractAndParseBlock(xmlContent, "microapp") { blockContent ->
                microappParser.parseMicroapp(blockContent)
            }
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге microapp", e)
            null
        }
    }

    /**
     * Извлекает и парсит стили из полного XML микроаппа
     */
    private fun parseStylesFromFullXml(xmlContent: String): AllStyles? {
        return try {
            extractAndParseBlock(xmlContent, "allStyles") { blockContent ->
                styleParser.parseStyles(blockContent)
            }
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге styles", e)
            null
        }
    }

    /**
     * Извлекает и парсит события из полного XML микроаппа
     */
    private fun parseEventsFromFullXml(xmlContent: String): AllEvents? {
        return try {
            extractAndParseBlock(xmlContent, "allEvents") { blockContent ->
                eventParser.parseEvents(blockContent)
            }
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге events", e)
            null
        }
    }

    /**
     * Извлекает и парсит действия событий из полного XML микроаппа
     */
    private fun parseEventActionsFromFullXml(xmlContent: String): AllEventActions? {
        return try {
            extractAndParseBlock(xmlContent, "allEventActions") { blockContent ->
                eventParser.parseEventActions(blockContent)
            }
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге eventActions", e)
            null
        }
    }

    /**
     * Извлекает и парсит экраны из полного XML микроаппа
     */
    private fun parseScreensFromFullXml(xmlContent: String): List<Screen> {
        return try {
            extractAndParseBlock(xmlContent, "screens") { blockContent ->
                screenParser.parseScreens(blockContent)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге screens", e)
            emptyList()
        }
    }

    /**
     * Извлекает и парсит запросы из полного XML микроаппа
     */
    private fun parseQueriesFromFullXml(xmlContent: String): List<Query> {
        return try {
            extractAndParseBlock(xmlContent, "allQueries") { blockContent ->
                queryParser.parseQueries(blockContent)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге queries", e)
            emptyList()
        }
    }

    /**
     * Извлекает и парсит экранные запросы из полного XML микроаппа
     */
    private fun parseScreenQueriesFromFullXml(xmlContent: String): List<ScreenQuery> {
        return try {
            extractAndParseBlock(xmlContent, "screenQueries") { blockContent ->
                queryParser.parseScreenQueries(blockContent)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге screenQueries", e)
            emptyList()
        }
    }

    /**
     * Извлекает и парсит виджеты из полного XML микроаппа
     */
    private fun parseWidgetsFromFullXml(xmlContent: String): List<Widget> {
        return try {
            extractAndParseBlock(xmlContent, "allWidgets") { blockContent ->
                widgetParser.parseWidgets(blockContent)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге widgets", e)
            emptyList()
        }
    }

    /**
     * Извлекает и парсит лэйауты из полного XML микроаппа
     */
    private fun parseLayoutsFromFullXml(xmlContent: String): List<Layout> {
        return try {
            extractAndParseBlock(xmlContent, "allLayouts") { blockContent ->
                layoutParser.parseLayouts(blockContent)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге layouts", e)
            emptyList()
        }
    }

    /**
     * Универсальный метод для извлечения и парсинга блока XML
     *
     * @param xmlContent Полный XML
     * @param blockName Имя блока (например, "screens")
     * @param parser Функция-парсер для блока
     * @return Результат парсинга или null если блок не найден
     */
    private inline fun <T> extractAndParseBlock(
        xmlContent: String,
        blockName: String,
        parser: (String) -> T
    ): T? {
        val startTag = "<$blockName>"
        val endTag = "</$blockName>"

        val startIndex = xmlContent.indexOf(startTag)
        if (startIndex == -1) {
            Log.d("SDUIParser", "Блок <$blockName> не найден в XML")
            return null
        }

        val endIndex = xmlContent.indexOf(endTag, startIndex)
        if (endIndex == -1) {
            Log.w("SDUIParser", "Не найден закрывающий тег для блока <$blockName>")
            return null
        }

        val blockContent = xmlContent.substring(startIndex, endIndex + endTag.length)
        Log.d("SDUIParser", "Извлечен блок <$blockName>, размер: ${blockContent.length} символов")

        return try {
            parser(blockContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге блока <$blockName>", e)
            null
        }
    }
}