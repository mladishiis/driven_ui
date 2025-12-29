package com.example.drivenui.parser

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.binding.*
import com.example.drivenui.parser.models.*
import com.example.drivenui.parser.parsers.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Главный парсер с поддержкой новой структуры компонентов и макросов
 */
class SDUIParser(private val context: Context) {

    private val styleParser = StyleParser()
    private val eventParser = EventParser()
    private val queryParser = QueryParser()
    private val screenQueryParser = ScreenQueryParser()
    private val microappParser = MicroappParser()
    private val widgetParser = WidgetParser()
    private val layoutParser = LayoutParser()
    private val componentParser = ComponentParser()
    private val bindingEngine = BindingEngine()
    private val jsonDataLoader = JsonDataLoader(context)

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
            Log.d("ParsedMicroappResult", "Виджеты: ${widgets.size}")
            Log.d("ParsedMicroappResult", "Лэйауты: ${layouts.size}")
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

        /**
         * Находит компонент по коду
         */
        fun findComponent(screenCode: String, componentCode: String): Component? {
            val screen = getScreenByCode(screenCode) ?: return null
            return findComponentRecursive(screen.rootComponent, componentCode)
        }

        private fun findComponentRecursive(component: Component?, targetCode: String): Component? {
            if (component == null) return null
            if (component.code == targetCode) return component
            return component.children.firstNotNullOfOrNull {
                findComponentRecursive(it, targetCode)
            }
        }

        /**
         * Получает разрешенное значение свойства
         */
        fun getResolvedPropertyValue(
            screenCode: String,
            componentCode: String,
            propertyCode: String
        ): String {
            val component = findComponent(screenCode, componentCode) ?: return ""
            return component.properties.find { it.code == propertyCode }?.resolvedValue ?: ""
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
     * Парсит микроапп с поддержкой данных и макросов (исправленная версия)
     */
    fun parseWithDataBinding(
        fileName: String,
        jsonFileNames: List<String> = emptyList(),
        queryResults: Map<String, Any> = emptyMap(),
        screenQueryResults: Map<String, Any> = emptyMap(),
        appState: Map<String, Any> = emptyMap(),
        localVariables: Map<String, Any> = emptyMap()
    ): ParsedMicroappResult {
        return try {
            Log.d("SDUIParser", "Парсинг с data binding: $fileName")
            Log.d("SDUIParser", "JSON файлы: $jsonFileNames")

            // Базовый парсинг XML
            val baseResult = parseFromAssetsNew(fileName)

            if (baseResult.screens.isEmpty()) {
                Log.e("SDUIParser", "Не найдены экраны в результате парсинга")
                return baseResult
            }

            // Загружаем JSON данные
            val jsonData = jsonDataLoader.loadJsonFiles(jsonFileNames)

            // Конвертируем Map<String, Any> в Map<String, JSONArray> для DataContext
            val jsonArrays = mutableMapOf<String, JSONArray>()
            jsonData.forEach { (key, value) ->
                when (value) {
                    is JSONArray -> jsonArrays[key] = value
                    is JSONObject -> {
                        // Если это JSONObject, проверяем, есть ли в нем массивы
                        // и добавляем их как отдельные источники
                        value.keys().forEach { objKey ->
                            val objValue = value.get(objKey)
                            if (objValue is JSONArray) {
                                // Используем комбинированный ключ: fileName.objectKey
                                jsonArrays["$key.$objKey"] = objValue
                            }
                        }
                    }
                }
            }

            // Логируем screen queries для отладки
            Log.d("SDUIParser", "ScreenQueries из XML: ${baseResult.screenQueries.size}")
            baseResult.screenQueries.forEach { screenQuery ->
                Log.d("SDUIParser", "  - ${screenQuery.code} -> ${screenQuery.queryCode} для экрана ${screenQuery.screenCode}")
            }

            // Создаем контекст данных с screenQueryResults
            val dataContext = DataContext(
                jsonSources = jsonArrays,
                queryResults = queryResults,
                screenQueryResults = screenQueryResults,
                appState = appState,
                localVariables = localVariables
            )

            // Применяем биндинги ко всем экранам
            val boundScreens = baseResult.screens.map { screen ->
                try {
                    val boundScreen = bindScreen(screen, dataContext)
                    // Проверяем результат биндинга
                    boundScreen.rootComponent?.let { root ->
                        val bindingCount = countBindingsInComponent(root)
                        val resolvedCount = countResolvedBindings(root)
                        Log.d("SDUIParser", "Экран ${screen.screenCode}: применено $resolvedCount из $bindingCount биндингов")
                    }
                    boundScreen
                } catch (e: Exception) {
                    Log.e("SDUIParser", "Ошибка при биндинге экрана ${screen.screenCode}", e)
                    screen // Возвращаем исходный экран в случае ошибки
                }
            }

            Log.d("SDUIParser", "Биндинг данных завершен: ${boundScreens.size} экранов")

            baseResult.copy(
                screens = boundScreens,
                dataContext = dataContext
            )
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге с data binding", e)
            ParsedMicroappResult()
        }
    }

    /**
     * Биндит данные к экрану (улучшенная версия с отладкой)
     */
    private fun bindScreen(
        screen: ParsedScreen,
        context: DataContext
    ): ParsedScreen {
        return try {
            if (screen.rootComponent == null) {
                Log.w("SDUIParser", "Экран ${screen.screenCode} не имеет корневого компонента")
                return screen
            }

            // Проверяем, есть ли биндинги в экране
            val bindingCount = countBindingsInComponent(screen.rootComponent)
            if (bindingCount == 0) {
                Log.d("SDUIParser", "Экран ${screen.screenCode}: биндинги не найдены")
                return screen
            }

            Log.d("SDUIParser", "Экран ${screen.screenCode}: найдено $bindingCount биндингов")

            // Применяем биндинги
            val boundComponent = bindingEngine.bindComponent(screen.rootComponent, context)

            // Проверяем результат
            val resolvedBindings = countResolvedBindings(boundComponent)
            Log.d("SDUIParser", "Экран ${screen.screenCode}: разрешено $resolvedBindings из $bindingCount биндингов")

            screen.copy(rootComponent = boundComponent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при биндинге экрана ${screen.screenCode}", e)
            screen
        }
    }

    /**
     * Подсчитывает разрешенные биндинги в компоненте
     */
    private fun countResolvedBindings(component: Component): Int {
        var resolved = 0

        fun countRecursive(comp: Component) {
            comp.properties.forEach { property ->
                if (property.hasBindings && property.resolvedValue != property.rawValue) {
                    resolved++
                }
            }
            comp.children.forEach { child ->
                countRecursive(child)
            }
        }

        countRecursive(component)
        return resolved
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
            val screens = componentParser.parseAllComponentsFromFullXml(xmlContent)
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

            // Логируем структуру компонентов и биндинги
            screens.forEachIndexed { index, screen ->
                Log.d("SDUIParser", "Экран $index: ${screen.title}")
                screen.rootComponent?.let {
                    val bindingCount = countBindingsInComponent(it)
                    Log.d("SDUIParser", "  Корневой компонент: ${it.title} с ${it.children.size} детьми")
                    Log.d("SDUIParser", "  Биндингов в компоненте: $bindingCount")

                    // Выводим информацию о биндингах для отладки
                    if (bindingCount > 0) {
                        logComponentBindings(it)
                    }
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
     * Динамическое обновление данных экрана
     */
    fun updateScreenData(
        screen: ParsedScreen,
        newData: Map<String, Any>,
        updateType: BindingSourceType = BindingSourceType.SCREEN_QUERY_RESULT
    ): ParsedScreen {
        return try {
            // Создаем базовый контекст
            val baseContext = DataContext()

            // Обновляем контекст в зависимости от типа
            val updatedContext = when (updateType) {
                BindingSourceType.SCREEN_QUERY_RESULT -> baseContext.copy(screenQueryResults = newData)
                BindingSourceType.QUERY_RESULT -> baseContext.copy(queryResults = newData)
                BindingSourceType.JSON_FILE -> {
                    // Фильтруем только JSONArray для jsonSources
                    val jsonArrays = newData.filterValues { it is JSONArray } as Map<String, JSONArray>
                    baseContext.copy(jsonSources = jsonArrays)
                }
                BindingSourceType.APP_STATE -> baseContext.copy(appState = newData)
                BindingSourceType.LOCAL_VAR -> baseContext.copy(localVariables = newData)
                else -> baseContext
            }

            bindScreen(screen, updatedContext)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при обновлении данных экрана", e)
            screen
        }
    }

    /**
     * Специальный метод для парсинга экрана carriers с mock данными
     */
    fun parseCarriersScreenWithMockData(): ParsedScreen? {
        return try {
            // Создаем mock данные для carriers_allCarriers
            val mockCarriersData = JSONArray().apply {
                for (i in 0..4) {
                    put(JSONObject().apply {
                        put("carrierName", "Перевозчик ${i + 1}")
                        put("id", "carrier_$i")
                        put("status", "active")
                    })
                }
            }

            // Парсим с mock данными
            val result = parseWithDataBinding(
                fileName = "microapp.xml",
                screenQueryResults = mapOf(
                    "carriers_allCarriers" to mockCarriersData
                )
            )

            val carriersScreen = result.getScreenByCode("carriers")

            if (carriersScreen != null) {
                Log.d("SDUIParser", "Найден экран carriers")

                // Проверяем биндинги
                carriersScreen.rootComponent?.let { root ->
                    val bindingCount = countBindingsInComponent(root)
                    Log.d("SDUIParser", "Биндингов в экране carriers: $bindingCount")

                    // Выводим значения
                    result.getResolvedValues().forEach { (key, value) ->
                        if (key.contains("carriers_list")) {
                            Log.d("SDUIParser", "Разрешенное значение $key: $value")
                        }
                    }
                }
            }

            carriersScreen
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при парсинге экрана carriers с mock данными", e)
            null
        }
    }

    /**
     * Находит все компоненты с биндингами в экране
     */
    fun findComponentsWithBindings(screen: ParsedScreen): List<Component> {
        val components = mutableListOf<Component>()

        fun searchComponents(component: Component) {
            if (component.properties.any { it.hasBindings }) {
                components.add(component)
            }
            component.children.forEach { child ->
                searchComponents(child)
            }
        }

        screen.rootComponent?.let { searchComponents(it) }
        return components
    }

    /**
     * Получает значения биндингов для компонента
     */
    fun getComponentBindings(component: Component): Map<String, String> {
        val bindings = mutableMapOf<String, String>()
        component.properties.forEach { property ->
            if (property.hasBindings) {
                bindings[property.code] = property.resolvedValue
            }
        }
        return bindings
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
            screenQueryParser.parseScreenQueries(xmlContent)
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

    /**
     * Вспомогательная функция для извлечения блока XML
     */
    private inline fun <T> extractAndParseBlock(
        xmlContent: String,
        blockName: String,
        parser: (String) -> T
    ): T? {
        return try {
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

            parser(blockContent)
        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка при извлечении и парсинге блока <$blockName>", e)
            null
        }
    }
}