package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.Query
import com.example.drivenui.parser.models.QueryProperty
import com.example.drivenui.parser.models.ScreenQuery
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class QueryParser {

    fun parseAllQueries(xmlContent: String): List<Query> {
        Log.d("QueryParser", "Начинаем парсинг allQueries")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val queries = mutableListOf<Query>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "query" -> {
                            try {
                                val query = parseQuery(parser)
                                if (query.code.isNotEmpty()) {
                                    queries.add(query)
                                    Log.d("QueryParser", "Успешно распарсен query: ${query.title} (${query.code})")
                                }
                            } catch (e: Exception) {
                                Log.e("QueryParser", "Ошибка при парсинге query", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("QueryParser", "Найдено запросов: ${queries.size}")
        return queries
    }

    private fun parseQuery(parser: XmlPullParser): Query {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var type = ""
        var endpoint = ""
        var mockFile: String? = null
        val properties = mutableListOf<QueryProperty>()

        Log.d("QueryParser", "Парсинг query: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "query")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "type" -> type = parser.nextText()
                        "endpoint" -> endpoint = parser.nextText()
                        "mockFile" -> mockFile = parser.nextText()
                        "properties" -> parseQueryProperties(parser, properties)
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return Query(
            title = title,
            code = code,
            type = type,
            endpoint = endpoint,
            mockFile = mockFile,
            properties = properties
        )
    }

    private fun parseQueryProperties(parser: XmlPullParser, properties: MutableList<QueryProperty>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            val property = parseQueryProperty(parser)
                            if (property.paramType.isNotEmpty()) {
                                properties.add(property)
                            }
                        }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseQueryProperty(parser: XmlPullParser): QueryProperty {
        var paramType = ""
        var variableName = ""
        var variableValue = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> paramType = parser.nextText()
                        "variableName" -> variableName = parser.nextText()
                        "variableValue" -> variableValue = parser.nextText()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return QueryProperty(paramType, variableName, variableValue)
    }

    fun parseScreenQueries(xmlContent: String): List<ScreenQuery> {
        Log.d("QueryParser", "Начинаем парсинг screenQueries")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val screenQueries = mutableListOf<ScreenQuery>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenQuery" -> {
                            try {
                                val screenQuery = parseScreenQuery(parser)
                                if (screenQuery.code.isNotEmpty()) {
                                    screenQueries.add(screenQuery)
                                    Log.d("QueryParser", "Успешно распарсен screenQuery: ${screenQuery.code}")
                                }
                            } catch (e: Exception) {
                                Log.e("QueryParser", "Ошибка при парсинге screenQuery", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("QueryParser", "Найдено screenQueries: ${screenQueries.size}")
        return screenQueries
    }

    private fun parseScreenQuery(parser: XmlPullParser): ScreenQuery {
        var code = ""
        var screenCode = ""
        var queryCode = ""
        var order = 0
        val propertiesMap = mutableMapOf<String, String>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenQuery")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "screenCode" -> screenCode = parser.nextText()
                        "screenСode" -> screenCode = parser.nextText() // Для обработки опечатки
                        "queryCode" -> queryCode = parser.nextText()
                        "order" -> {
                            val orderText = parser.nextText()
                            order = orderText.toIntOrNull() ?: 0
                        }
                        "properties" -> parseScreenQueryPropertiesToMap(parser, propertiesMap)
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return ScreenQuery(
            code = code,
            screenCode = screenCode,
            queryCode = queryCode,
            properties = propertiesMap,
            order = order
        )
    }

    private fun parseScreenQueryPropertiesToMap(parser: XmlPullParser, map: MutableMap<String, String>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            val (variableName, variableValue) = parseScreenQueryPropertyToMap(parser)
                            if (variableName.isNotEmpty()) {
                                map[variableName] = variableValue
                            }
                        }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseScreenQueryPropertyToMap(parser: XmlPullParser): Pair<String, String> {
        var variableName = ""
        var variableValue = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "variableName" -> variableName = parser.nextText()
                        "variableValue" -> variableValue = parser.nextText()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return Pair(variableName, variableValue)
    }

    private fun skipCurrentTag(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }
}