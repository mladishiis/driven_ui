package com.example.drivenui.parser.parsers

import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер запросов API из XML формата Driven UI
 *
 * Поддерживает парсинг:
 * - Общих запросов (allQueries)
 * - Запросов экранов (screenQueries)
 * - Свойств и условий запросов
 */
class QueryParser {

    /**
     * Парсит все запросы API из XML строки
     *
     * @param xmlContent XML строка содержащая блок <allQueries>
     * @return Список [Query] объектов
     */
    fun parseQueries(xmlContent: String): List<Query> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val queries = mutableListOf<Query>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "query" -> {
                            queries.add(parseQuery(parser))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return queries
    }

    /**
     * Парсит запросы экранов из XML строки
     *
     * @param xmlContent XML строка содержащая блок <screenQueries>
     * @return Список [ScreenQuery] объектов
     */
    fun parseScreenQueries(xmlContent: String): List<ScreenQuery> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val screenQueries = mutableListOf<ScreenQuery>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenQuery" -> {
                            screenQueries.add(parseScreenQuery(parser))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return screenQueries
    }

    /**
     * Парсит отдельный запрос API из XML
     *
     * @param parser XmlPullParser позиционированный на теге <query>
     * @return [Query] объект запроса
     */
    private fun parseQuery(parser: XmlPullParser): Query {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var type = ""
        var endpoint = ""
        val properties = mutableListOf<QueryProperty>()
        val conditions = mutableListOf<QueryCondition>()

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "query")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "type" -> type = parser.nextText()
                        "endpoint" -> endpoint = parser.nextText()
                        "property" -> properties.add(parseQueryProperty(parser))
                        "condition" -> conditions.add(parseQueryCondition(parser))
                    }
                }
            }
            eventType = parser.next()
        }

        return Query(title, code, type, endpoint, properties, conditions)
    }

    /**
     * Парсит отдельный экранный запрос из XML
     *
     * @param parser XmlPullParser позиционированный на теге <screenQuery>
     * @return [ScreenQuery] объект экранного запроса
     */
    private fun parseScreenQuery(parser: XmlPullParser): ScreenQuery {
        var code = ""
        var screenCode = ""
        var queryCode = ""
        var order = 0
        val properties = mutableListOf<QueryProperty>()
        val conditions = mutableListOf<QueryCondition>()

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenQuery")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "screenСode" -> screenCode = parser.nextText()
                        "queryCode" -> queryCode = parser.nextText()
                        "order" -> order = parser.nextText().toIntOrNull() ?: 0
                        "property" -> properties.add(parseQueryProperty(parser))
                        "condition" -> conditions.add(parseQueryCondition(parser))
                    }
                }
            }
            eventType = parser.next()
        }

        return ScreenQuery(code, screenCode, queryCode, order, properties, conditions)
    }

    /**
     * Парсит свойство запроса из XML
     *
     * @param parser XmlPullParser позиционированный на теге <property> внутри запроса
     * @return [QueryProperty] объект свойства запроса
     */
    private fun parseQueryProperty(parser: XmlPullParser): QueryProperty {
        var code = ""
        var variableName = ""
        var variableValue = ""

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "variableName" -> variableName = parser.nextText()
                        "variableValue" -> variableValue = parser.nextText()
                    }
                }
            }
            eventType = parser.next()
        }

        return QueryProperty(code, variableName, variableValue)
    }

    /**
     * Парсит условие запроса из XML
     *
     * @param parser XmlPullParser позиционированный на теге <condition>
     * @return [QueryCondition] объект условия запроса
     */
    private fun parseQueryCondition(parser: XmlPullParser): QueryCondition {
        var code = ""
        var value = ""

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "condition")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "value" -> value = parser.nextText()
                    }
                }
            }
            eventType = parser.next()
        }

        return QueryCondition(code, value)
    }

    /**
     * Парсит весь блок screenQueries из полного XML микроаппа
     *
     * @param xmlContent Полное XML содержимое микроаппа
     * @return Список [ScreenQuery] объектов
     */
    fun parseScreenQueriesFromFullXml(xmlContent: String): List<ScreenQuery> {
        return try {
            val start = xmlContent.indexOf("<screenQueries>")
            val end = xmlContent.indexOf("</screenQueries>") + "</screenQueries>".length
            if (start != -1 && end != -1) {
                val screenQueriesXml = xmlContent.substring(start, end)
                parseScreenQueries(screenQueriesXml)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}