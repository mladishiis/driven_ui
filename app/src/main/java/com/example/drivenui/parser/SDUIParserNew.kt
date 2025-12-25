package com.example.drivenui.parser

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.binding.*
import com.example.drivenui.parser.models.*
import com.example.drivenui.parser.parsers.*
import org.json.JSONArray

/**
 * Главный парсер с поддержкой новой структуры компонентов и макросов
 */
class SDUIParserNew(private val context: Context) {

    private val styleParser = StyleParser()
    private val eventParser = EventParser()
    private val queryParser = QueryParser()
    private val microappParser = MicroappParser()
    private val widgetParser = WidgetParser()
    private val layoutParser = LayoutParser()
    private val componentParser = ComponentParser()
    private val bindingEngine = BindingEngine()
    private val jsonDataLoader = JsonDataLoader(context)
    private val bindingParser = BindingParser()

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
        val dataContext: DataContext? = null  // Добавляем контекст данных
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
                    values[component.code + "." + property.code] = property.resolvedValue
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
     * Парсит микроапп с поддержкой данных и макросов (исправленная версия)
     */
    fun parseWithDataBinding(
        fileName: String,
        jsonFileNames: List<String> = emptyList(),
        queryResults: Map<String, Any> = emptyMap(),
        appState: Map<String, Any> = emptyMap(),
        localVariables: Map<String, Any> = emptyMap()
    ): ParsedMicroappResult {
        return try {
            Log.d("SDUIParserNew", "Парсинг с data binding: $fileName")
            Log.d("SDUIParserNew", "JSON файлы: $jsonFileNames")

            // Базовый парсинг XML
            val baseResult = parseFromAssetsNew(fileName)

            if (baseResult.screens.isEmpty()) {
                Log.e("SDUIParserNew", "Не найдены экраны в результате парсинга")
                return baseResult
            }

            // Загружаем JSON данные
            val jsonData = jsonDataLoader.loadJsonFiles(jsonFileNames)

            // Логируем загруженные данные
            jsonData.forEach { (key, value) ->
                Log.d("SDUIParserNew", "Загружен JSON: $key, элементов: ${value.length()}")
                if (value.length() > 0) {
                    Log.d("SDUIParserNew", "  Пример первого элемента: ${value.optJSONObject(0)?.toString()?.take(100)}")
                }
            }

            // Создаем контекст данных
            val dataContext = DataContext(
                jsonSources = jsonData,
                queryResults = queryResults,
                appState = appState,
                localVariables = localVariables
            )

            // Применяем биндинги ко всем экранам
            val boundScreens = baseResult.screens.mapNotNull { screen ->
                try {
                    val boundScreen = bindScreen(screen, dataContext)
                    // Проверяем результат биндинга
                    boundScreen.rootComponent?.let { root ->
                        val bindingCount = countBindingsInComponent(root)
                        if (bindingCount > 0) {
                            Log.d("SDUIParserNew", "Экран ${screen.screenCode}: применено биндингов: $bindingCount")
                        }
                    }
                    boundScreen
                } catch (e: Exception) {
                    Log.e("SDUIParserNew", "Ошибка при биндинге экрана ${screen.screenCode}", e)
                    screen // Возвращаем исходный экран в случае ошибки
                }
            }

            Log.d("SDUIParserNew", "Биндинг данных завершен: ${boundScreens.size} экранов")

            baseResult.copy(
                screens = boundScreens,
                dataContext = dataContext
            )
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при парсинге с data binding", e)
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
                Log.w("SDUIParserNew", "Экран ${screen.screenCode} не имеет корневого компонента")
                return screen
            }

            // Проверяем, есть ли биндинги в экране
            val bindingCount = countBindingsInComponent(screen.rootComponent)
            if (bindingCount == 0) {
                Log.d("SDUIParserNew", "Экран ${screen.screenCode}: биндинги не найдены")
                return screen
            }

            Log.d("SDUIParserNew", "Экран ${screen.screenCode}: найдено $bindingCount биндингов")

            // Применяем биндинги
            val boundComponent = bindingEngine.bindComponent(screen.rootComponent, context)

            // Проверяем результат
            val resolvedBindings = countResolvedBindings(boundComponent)
            Log.d("SDUIParserNew", "Экран ${screen.screenCode}: разрешено $resolvedBindings из $bindingCount биндингов")

            screen.copy(rootComponent = boundComponent)
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при биндинге экрана ${screen.screenCode}", e)
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

            // Логируем структуру компонентов и биндинги
            screens.forEachIndexed { index, screen ->
                Log.d("SDUIParserNew", "Экран $index: ${screen.title}")
                screen.rootComponent?.let {
                    val bindingCount = countBindingsInComponent(it)
                    Log.d("SDUIParserNew", "  Корневой компонент: ${it.title} с ${it.children.size} детьми")
                    Log.d("SDUIParserNew", "  Биндингов в компоненте: $bindingCount")

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
            Log.e("SDUIParserNew", "Ошибка при парсинге полного XML", e)
            ParsedMicroappResult()
        }
    }

    /**
     * Подсчитывает биндинги в компоненте
     */
    private fun countBindingsInComponent(component: Component): Int {
        return component.properties.sumOf { it.bindings.size } +
                component.children.sumOf { countBindingsInComponent(it) }
    }

    /**
     * Логирует биндинги компонента для отладки
     */
    private fun logComponentBindings(component: Component, indent: String = "  ") {
        component.properties.forEach { property ->
            if (property.hasBindings) {
                Log.d("SDUIParserNew", "$indent${component.code}.${property.code}:")
                Log.d("SDUIParserNew", "$indent  rawValue: ${property.rawValue}")
                Log.d("SDUIParserNew", "$indent  resolvedValue: ${property.resolvedValue}")
                property.bindings.forEachIndexed { index, binding ->
                    Log.d("SDUIParserNew", "$indent  binding[$index]: ${binding.expression}")
                    Log.d("SDUIParserNew", "$indent    source: ${binding.sourceName}.${binding.path}")
                    Log.d("SDUIParserNew", "$indent    type: ${binding.sourceType}")
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
        updateType: BindingSourceType = BindingSourceType.QUERY_RESULT
    ): ParsedScreen {
        return try {
            // Создаем базовый контекст
            val baseContext = DataContext()

            // Обновляем контекст в зависимости от типа
            val updatedContext = when (updateType) {
                BindingSourceType.QUERY_RESULT -> baseContext.copy(queryResults = newData)
                BindingSourceType.JSON_FILE -> {
                    val jsonData = newData.filterValues { it is JSONArray } as Map<String, JSONArray>
                    baseContext.copy(jsonSources = jsonData)
                }
                BindingSourceType.APP_STATE -> baseContext.copy(appState = newData)
                BindingSourceType.LOCAL_VAR -> baseContext.copy(localVariables = newData)
                else -> baseContext
            }

            bindScreen(screen, updatedContext)
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при обновлении данных экрана", e)
            screen
        }
    }

    /**
     * Парсит специфический экран с данными (например, carriers)
     */
    fun parseCarriersScreenWithData(): ParsedScreen? {
        return try {
            val result = parseWithDataBinding(
                fileName = "microapp_tavrida.xml",
                jsonFileNames = listOf("carriers_list.json")
            )

            // Находим экран carriers
            val carriersScreen = result.getScreenByCode("carriers")

            if (carriersScreen != null) {
                Log.d("SDUIParserNew", "Найден экран carriers")

                // Проверяем биндинги
                carriersScreen.rootComponent?.let { root ->
                    val bindingCount = countBindingsInComponent(root)
                    Log.d("SDUIParserNew", "Биндингов в экране carriers: $bindingCount")

                    // Выводим примеры подставленных значений
                    result.getResolvedValues().forEach { (key, value) ->
                        if (key.contains("carriers_list")) {
                            Log.d("SDUIParserNew", "Разрешенное значение $key: $value")
                        }
                    }
                }
            }

            carriersScreen
        } catch (e: Exception) {
            Log.e("SDUIParserNew", "Ошибка при парсинге экрана carriers с данными", e)
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

/**
 * Загрузчик JSON данных
 */
class JsonDataLoader(private val context: Context) {

    /**
     * Загружает несколько JSON файлов
     */
    fun loadJsonFiles(fileNames: List<String>): Map<String, JSONArray> {
        val jsonData = mutableMapOf<String, JSONArray>()

        fileNames.forEach { fileName ->
            try {
                val data = loadJsonData(fileName)
                if (data != null) {
                    // Используем имя файла без расширения как ключ
                    val key = fileName.removeSuffix(".json")
                    jsonData[key] = data
                    Log.d("JsonDataLoader", "Загружен JSON файл: $fileName -> $key")
                }
            } catch (e: Exception) {
                Log.e("JsonDataLoader", "Ошибка загрузки файла $fileName", e)
            }
        }

        return jsonData
    }

    /**
     * Загружает один JSON файл
     */
    fun loadJsonData(fileName: String): JSONArray? {
        return try {
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        } catch (e: Exception) {
            Log.e("JsonDataLoader", "Ошибка загрузки JSON файла: $fileName", e)
            null
        }
    }
}