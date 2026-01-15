package com.example.drivenui.parser

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.models.AllEventActions
import com.example.drivenui.parser.models.AllEvents
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ColorStyle
import com.example.drivenui.parser.models.Component
import com.example.drivenui.parser.models.DataContext
import com.example.drivenui.parser.models.Layout
import com.example.drivenui.parser.models.Microapp
import com.example.drivenui.parser.models.ParsedScreen
import com.example.drivenui.parser.models.Query
import com.example.drivenui.parser.models.ScreenQuery
import com.example.drivenui.parser.models.TextStyle
import com.example.drivenui.parser.models.Widget
import com.example.drivenui.parser.parsers.ComponentParser
import com.example.drivenui.parser.parsers.EventParser
import com.example.drivenui.parser.parsers.LayoutParser
import com.example.drivenui.parser.parsers.MicroappParser
import com.example.drivenui.parser.parsers.QueryParser
import com.example.drivenui.parser.parsers.StyleParser
import com.example.drivenui.parser.parsers.WidgetParser

/**
 * Главный парсер с поддержкой новой структуры компонентов и макросов
 */
class SDUIParser(private val context: Context) {

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
        val layouts: List<Layout> = emptyList(),
        val dataContext: DataContext? = null
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
         * Получает экран по коду
         */
        fun getScreenByCode(screenCode: String): ParsedScreen? {
            return screens.firstOrNull { it.screenCode == screenCode }
        }

        /**
         * Получает текстовые стили
         */
        fun getTextStyles(): List<TextStyle> = styles?.textStyles ?: emptyList()

        /**
         * Получает цветовые стили
         */
        fun getColorStyles(): List<ColorStyle> = styles?.colorStyles ?: emptyList()

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
         * Проверяет, есть ли биндинги в компоненте
         */
        private fun hasBindingsRecursive(component: Component): Boolean {
            return component.properties.any { it.hasBindings } ||
                    component.children.any { hasBindingsRecursive(it) }
        }

        /**
         * Подсчитывает общее количество биндингов
         */
        fun countAllBindings(): Int {
            var total = 0
            screens.forEach { screen ->
                screen.rootComponent?.let { root ->
                    total += countBindingsRecursive(root)
                }
            }
            return total
        }

        /**
         * Подсчитывает биндинги в компоненте
         */
        private fun countBindingsRecursive(component: Component): Int {
            var count = component.properties.sumOf { it.bindings.size }
            component.children.forEach { child ->
                count += countBindingsRecursive(child)
            }
            return count
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
                "bindingsCount" to countAllBindings(),
                "hasComponentStructure" to screens.any { it.rootComponent != null },
                "hasDataBinding" to screens.any { screen ->
                    screen.rootComponent?.let { hasBindingsRecursive(it) } == true
                }
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

                // Выводим информацию о запросах
                if (screen.requests.isNotEmpty()) {
                    Log.d("ParsedMicroappResult", "    Запросы (${screen.requests.size}):")
                    screen.requests.forEach { query ->
                        Log.d("ParsedMicroappResult", "      - ${query.code} → ${query.queryCode}")
                    }
                }

                if (screen.rootComponent != null) {
                    val bindingCount = countBindingsRecursive(screen.rootComponent)
                    Log.d("ParsedMicroappResult", "    Корневой компонент: ${screen.rootComponent.title}")
                    Log.d("ParsedMicroappResult", "    Компонентов: ${countComponentsRecursive(screen.rootComponent)}")
                    Log.d("ParsedMicroappResult", "    Биндингов: $bindingCount")
                }
            }
            Log.d("ParsedMicroappResult", "Стили текста: ${getTextStyles().size}")
            Log.d("ParsedMicroappResult", "Стили цвета: ${getColorStyles().size}")
            Log.d("ParsedMicroappResult", "События: ${events?.events?.size ?: 0}")
            Log.d("ParsedMicroappResult", "Действия событий: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("ParsedMicroappResult", "Запросы: ${queries.size}")
            Log.d("ParsedMicroappResult", "ScreenQueries: ${screenQueries.size}")
            Log.d("ParsedMicroappResult", "Виджеты: ${widgets.size}")
            Log.d("ParsedMicroappResult", "Лэйауты: ${layouts.size}")

            // Подсчет всех запросов по экранам
            val totalRequests = screens.sumOf { it.requests.size }
            Log.d("ParsedMicroappResult", "Всего запросов в экранах: $totalRequests")

            Log.d("ParsedMicroappResult", "Всего компонентов: ${countAllComponents()}")
            Log.d("ParsedMicroappResult", "Всего биндингов: ${countAllBindings()}")
            Log.d("ParsedMicroappResult", "=======================================")
        }

        /**
         * Рекурсивно подсчитывает компоненты
         */
        fun countComponentsRecursive(component: Component): Int {
            var count = 1 // текущий компонент
            component.children.forEach { child ->
                count += countComponentsRecursive(child)
            }
            return count
        }

        /**
         * Получает разрешенные значения для всех биндингов
         */
        fun getResolvedValues(): Map<String, String> {
            val values = mutableMapOf<String, String>()
            screens.forEach { screen ->
                collectResolvedValues(screen.rootComponent, values)
            }
            return values
        }

        private fun collectResolvedValues(component: Component?, values: MutableMap<String, String>) {
            if (component == null) return

            component.properties.forEach { property ->
                if (property.hasBindings) {
                    values["${component.code}.${property.code}"] = property.resolvedValue
                }
            }

            component.children.forEach { child ->
                collectResolvedValues(child, values)
            }
        }
    }

    /**
     * Загружает и парсит микроапп из файла в assets с новой структурой
     */
    fun parseFromAssetsNew(fileName: String): ParsedMicroappResult {
        return try {
            Log.d("SDUIParser", "Начинаем парсинг файла с новой структурой: $fileName")
            val inputStream = context.assets.open(fileName)
            val xmlContent = inputStream.bufferedReader().use { it.readText() }
            parseFullXmlContentNew(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при загрузке файла из assets: $fileName", e)
            ParsedMicroappResult()
        }
    }

    /**
     * Парсит полный XML микроаппа с новой структурой компонентов
     */
    private fun parseFullXmlContentNew(xmlContent: String): ParsedMicroappResult {
        return try {
            Log.d("SDUIParser", "Начинаем парсинг полного XML с новой структурой")

            val microapp = parseMicroappFromFullXml(xmlContent)
            val styles = parseStylesFromFullXml(xmlContent)
            val events = parseEventsFromFullXml(xmlContent)
            val eventActions = parseEventActionsFromFullXml(xmlContent)

            // 1. Парсим компоненты (экраны) - ПОКА БЕЗ ЗАПРОСОВ
            val screensWithoutQueries = componentParser.parseAllComponentsFromFullXml(xmlContent)

            // 2. Парсим запросы отдельно
            val queries = parseQueriesFromFullXml(xmlContent)
            val screenQueries = parseScreenQueriesFromFullXml(xmlContent)

            // 3. Группируем запросы по экранам для быстрого доступа
            val queriesByScreenCode = screenQueries.groupBy { it.screenCode }

            // 4. Обогащаем экраны запросами
            val screensWithQueries = screensWithoutQueries.map { screen ->
                val screenQueriesForThisScreen = queriesByScreenCode[screen.screenCode] ?: emptyList()

                // Создаем обогащенный ParsedScreen
                ParsedScreen(
                    title = screen.title,
                    screenCode = screen.screenCode,
                    screenShortCode = screen.screenShortCode,
                    deeplink = screen.deeplink,
                    rootComponent = screen.rootComponent,
                    requests = screenQueriesForThisScreen  // ← ДОБАВЛЯЕМ ЗАПРОСЫ
                )
            }

            val widgets = parseWidgetsFromFullXml(xmlContent)
            val layouts = parseLayoutsFromFullXml(xmlContent)

            Log.d("SDUIParser", "Парсинг завершен:")
            Log.d("SDUIParser", "  - microapp: ${microapp?.title ?: "не найден"}")
            Log.d("SDUIParser", "  - styles: ${styles != null}")
            Log.d("SDUIParser", "  - events: ${events?.events?.size ?: 0}")
            Log.d("SDUIParser", "  - eventActions: ${eventActions?.eventActions?.size ?: 0}")
            Log.d("SDUIParser", "  - screens: ${screensWithQueries.size}")
            Log.d("SDUIParser", "  - queries: ${queries.size}")
            Log.d("SDUIParser", "  - screenQueries: ${screenQueries.size}")
            Log.d("SDUIParser", "  - widgets: ${widgets.size}")
            Log.d("SDUIParser", "  - layouts: ${layouts.size}")

            // Логируем запросы для каждого экрана
            screensWithQueries.forEachIndexed { index, screen ->
                Log.d("SDUIParser", "Экран $index: ${screen.title}")
                if (screen.requests.isNotEmpty()) {
                    Log.d("SDUIParser", "  Запросы (${screen.requests.size}):")
                    screen.requests.forEach { query ->
                        Log.d("SDUIParser", "    - ${query.code} → ${query.queryCode}")
                    }
                } else {
                    Log.d("SDUIParser", "  Запросы: нет")
                }
            }

            // Создаем результат
            val result = ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                events = events,
                eventActions = eventActions,
                screens = screensWithQueries,  // ← ИСПОЛЬЗУЕМ ОБОГАЩЕННЫЕ ЭКРАНЫ
                queries = queries,
                screenQueries = screenQueries,
                widgets = widgets,
                layouts = layouts
            )

            // Логируем итоговую статистику
            result.logSummary()

            result
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге полного XML", e)
            ParsedMicroappResult()
        }
    }

    /**
     * Подсчитывает биндинги в компоненте
     */
    fun countBindingsInComponent(component: Component): Int {
        return component.properties.sumOf { it.bindings.size } +
                component.children.sumOf { countBindingsInComponent(it) }
    }

    /**
     * Логирует биндинги компонента для отладки
     */
    private fun logComponentBindings(component: Component, indent: String = "  ") {
        component.properties.forEach { property ->
            if (property.hasBindings) {
                Log.d("SDUIParser", "$indent${component.code}.${property.code}:")
                Log.d("SDUIParser", "$indent  rawValue: ${property.rawValue}")
                Log.d("SDUIParser", "$indent  resolvedValue: ${property.resolvedValue}")
                property.bindings.forEachIndexed { index, binding ->
                    Log.d("SDUIParser", "$indent  binding[$index]: ${binding.expression}")
                    Log.d("SDUIParser", "$indent    source: ${binding.sourceName}.${binding.path}")
                    Log.d("SDUIParser", "$indent    type: ${binding.sourceType}")
                }
            }
        }

        component.children.forEach { child ->
            logComponentBindings(child, "$indent  ")
        }
    }

    /**
     * Парсит список query из XML
     */
    private fun parseQueriesFromFullXml(xmlContent: String): List<Query> {
        return try {
            queryParser.parseAllQueries(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге queries", e)
            emptyList()
        }
    }

    /**
     * Парсит screen queries из XML
     */
    private fun parseScreenQueriesFromFullXml(xmlContent: String): List<ScreenQuery> {
        return try {
            queryParser.parseScreenQueries(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге screen queries", e)
            emptyList()
        }
    }

    /**
     * Парсит виджеты из XML
     */
    private fun parseWidgetsFromFullXml(xmlContent: String): List<Widget> {
        return try {
            widgetParser.parseWidgets(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге виджетов", e)
            emptyList()
        }
    }

    /**
     * Парсит лэйауты из XML
     */
    private fun parseLayoutsFromFullXml(xmlContent: String): List<Layout> {
        return try {
            layoutParser.parseLayouts(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге лэйаутов", e)
            emptyList()
        }
    }

    /**
     * Парсит микроапп из XML
     */
    private fun parseMicroappFromFullXml(xmlContent: String): Microapp? {
        return try {
            microappParser.parseMicroapp(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге микроаппа", e)
            null
        }
    }

    /**
     * Парсит стили из XML
     */
    private fun parseStylesFromFullXml(xmlContent: String): AllStyles? {
        return try {
            styleParser.parseStyles(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге стилей", e)
            null
        }
    }

    /**
     * Парсит события из XML
     */
    private fun parseEventsFromFullXml(xmlContent: String): AllEvents? {
        return try {
            eventParser.parseEvents(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге событий", e)
            null
        }
    }

    /**
     * Парсит действия событий из XML
     */
    private fun parseEventActionsFromFullXml(xmlContent: String): AllEventActions? {
        return try {
            eventParser.parseEventActions(xmlContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге действий событий", e)
            null
        }
    }
}