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
import com.example.drivenui.parser.parsers.MicroappParser
import com.example.drivenui.parser.parsers.QueryParser
import com.example.drivenui.parser.parsers.StyleParser
import java.io.File

/**
 * Главный парсер с поддержкой новой структуры компонентов и макросов
 */
class SDUIParser(private val context: Context) {

    private val styleParser = StyleParser()
    private val queryParser = QueryParser()
    private val microappParser = MicroappParser()
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
                "hasComponentStructure" to screens.any { it.rootComponent != null },
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
                    Log.d("ParsedMicroappResult", "    Корневой компонент: ${screen.rootComponent.title}")
                    Log.d("ParsedMicroappResult", "    Компонентов: ${countComponentsRecursive(screen.rootComponent)}")
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

            component.children.forEach { child ->
                collectResolvedValues(child, values)
            }
        }
    }

    fun parseFromDir(rootDir: File): ParsedMicroappResult {
        return try {
            Log.d("SDUIParser", "=== Парсинг новой структуры microapp из папки ===")

            val microappXml = File(rootDir, "microapp.xml").readText()
            val stylesXml = File(rootDir, "resources/allStyles.xml").readText()
            val queriesXml = File(rootDir, "queries/allQueries.xml").readText()

            val microapp = parseMicroappFromFullXml(microappXml)
            val styles = parseStylesFromFullXml(stylesXml)

            val screensWithoutQueries = parseScreensFromFolder(rootDir.resolve("screens"))

            val screenQueries = parseScreenQueriesFromScreensFolder(rootDir.resolve("screens"))

            val queriesByScreenCode = screenQueries.groupBy { it.screenCode }
            val screensWithQueries = screensWithoutQueries.map { screen ->
                screen.copy(requests = queriesByScreenCode[screen.screenCode].orEmpty())
            }

            val queries = parseQueriesFromFullXml(queriesXml)

            val result = ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                screens = screensWithQueries,
                queries = queries,
                screenQueries = screenQueries
            )

            result.logSummary()
            result

        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга microapp-структуры из папки", e)
            ParsedMicroappResult()
        }
    }

    /*fun parseFromAssetsRoot(): ParsedMicroappResult {
        return try {
            Log.d("SDUIParser", "=== Парсинг новой структуры microapp ===")

            // === ЧИТАЕМ ФАЙЛЫ (раздельно) ===
            val microappXml = readAsset("microapp.xml")
            val stylesXml = readAsset("resources/allStyles.xml")
            val queriesXml = readAsset("queries/allQueries.xml")

            // Парсим базовые блоки (как раньше, но из разных файлов) ===
            val microapp = parseMicroappFromFullXml(microappXml)
            val styles = parseStylesFromFullXml(stylesXml)

            // 1. Экраны (UI)
            val screensWithoutQueries = parseScreensFromFolder("screens")

            // 2. ScreenQueries (ОТДЕЛЬНО!)
            val screenQueries = parseScreenQueriesFromScreensFolder("screens")

            Log.d("DEBUG", "ScreenQueries parsed: ${screenQueries.size}")
            screenQueries.forEach {
                Log.d("DEBUG", "${it.screenCode} -> ${it.code}")
            }

            // 3. Группировка
            val queriesByScreenCode = screenQueries.groupBy { it.screenCode }

            // === 5. ТО САМОЕ ОБОГАЩЕНИЕ ЭКРАНОВ (ВАША ЛОГИКА) ===
            val screensWithQueries = screensWithoutQueries.map { screen ->
                screen.copy(
                    requests = queriesByScreenCode[screen.screenCode].orEmpty()
                )
            }

            // === 6. Парсим реестр обычных queries (как раньше) ===
            val queries = parseQueriesFromFullXml(queriesXml)

            // === 7. Собираем итоговый результат ===
            val result = ParsedMicroappResult(
                microapp = microapp,
                styles = styles,
                screens = screensWithQueries,
                queries = queries,
                screenQueries = screenQueries
            )

            result.logSummary()
            result

        } catch (e: Exception) {
            Log.e("SDUIParser", "Ошибка парсинга microapp-структуры", e)
            ParsedMicroappResult()
        }
    }*/


    private fun parseScreensFromFolder(folder: File): List<ParsedScreen> {
        val screens = mutableListOf<ParsedScreen>()
        val files = folder.listFiles()?.filter { it.extension == "xml" } ?: return emptyList()
        files.forEach { file ->
            val xml = file.readText()
            val screen = componentParser.parseSingleScreenXml(xml)
            screen?.let {
                screens.add(it)
                Log.d("SDUIParser", "Экран считан: ${it.screenCode}")
            }
        }
        return screens
    }

    private fun parseScreenQueriesFromScreensFolder(folder: File): List<ScreenQuery> {
        val result = mutableListOf<ScreenQuery>()
        val files = folder.listFiles()?.filter { it.extension == "xml" } ?: return emptyList()
        files.forEach { file ->
            val xml = file.readText()
            val queries = try {
                queryParser.parseScreenQueries(xml)
            } catch (e: Exception) {
                Log.e("SDUIParser", "Ошибка парсинга screenQueries в ${file.name}", e)
                emptyList()
            }
            result.addAll(queries)
        }
        return result
    }



    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    private fun parseScreenQueriesFromScreensFolder(folder: String): List<ScreenQuery> {
        val result = mutableListOf<ScreenQuery>()

        val files = context.assets.list(folder) ?: return emptyList()

        files
            .filter { it.endsWith(".xml") }
            .forEach { file ->
                val xml = readAsset("$folder/$file")

                val queries = try {
                    queryParser.parseScreenQueries(xml)
                } catch (e: Exception) {
                    Log.e("SDUIParser", "Ошибка парсинга screenQueries в $file", e)
                    emptyList()
                }

                if (queries.isNotEmpty()) {
                    Log.d(
                        "SDUIParser",
                        "Найдено screenQueries: ${queries.size} в экране $file"
                    )
                }

                result.addAll(queries)
            }

        return result
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
}