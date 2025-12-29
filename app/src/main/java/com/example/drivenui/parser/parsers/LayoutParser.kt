package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.Layout
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class LayoutParser {

    fun parseLayouts(xmlContent: String): List<Layout> {
        Log.d("LayoutParser", "Начинаем парсинг лэйаутов")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val layouts = mutableListOf<Layout>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "layout" -> {
                            try {
                                val layout = parseLayout(parser)
                                if (layout.code.isNotEmpty()) {
                                    layouts.add(layout)
                                    Log.d("LayoutParser", "Успешно распарсен layout: ${layout.title} (${layout.code})")
                                }
                            } catch (e: Exception) {
                                Log.e("LayoutParser", "Ошибка при парсинге layout", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("LayoutParser", "Найдено лэйаутов: ${layouts.size}")
        return layouts
    }

    private fun parseLayout(parser: XmlPullParser): Layout {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        val propertiesMap = mutableMapOf<String, String>()

        Log.d("LayoutParser", "Парсинг layout: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "layout")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "properties" -> parsePropertiesToMap(parser, propertiesMap)
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return Layout(title, code, propertiesMap)
    }

    private fun parsePropertiesToMap(parser: XmlPullParser, map: MutableMap<String, String>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            val property = parsePropertyToMap(parser)
                            if (property.first.isNotEmpty()) {
                                map[property.first] = property.second
                            }
                        }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parsePropertyToMap(parser: XmlPullParser): Pair<String, String> {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "value" -> value = parser.nextText()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return Pair(code, value)
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