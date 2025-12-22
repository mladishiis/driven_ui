package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.EventProperty
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
                                } else {
                                    Log.w("LayoutParser", "Пропущен layout с пустым кодом: ${layout.title}")
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
        layouts.forEachIndexed { index, layout ->
            Log.d("LayoutParser", "  $index: ${layout.title} (${layout.code}) - ${layout.properties.size} свойств")
        }
        return layouts
    }

    private fun parseLayout(parser: XmlPullParser): Layout {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        val properties = mutableListOf<EventProperty>()

        Log.d("LayoutParser", "Парсинг layout: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "layout")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("LayoutParser", "  code: $code")
                        }
                        "properties" -> {
                            // Парсим вложенный блок properties
                            properties.addAll(parseProperties(parser))
                            Log.d("LayoutParser", "  properties: ${properties.size}")
                        }
                        "property" -> {
                            // Если property напрямую внутри layout (не в блоке properties)
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("LayoutParser", "  property: ${property.code} = ${property.value}")
                            } catch (e: Exception) {
                                Log.e("LayoutParser", "Ошибка при парсинге property", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        else -> {
                            // Пропускаем неизвестные теги
                            Log.d("LayoutParser", "Пропущен тег в layout: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Layout(title, code, properties)
    }

    /**
     * Парсит вложенные properties из блока <properties>
     */
    private fun parseProperties(parser: XmlPullParser): List<EventProperty> {
        val properties = mutableListOf<EventProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("LayoutParser", "    property в properties: ${property.code}")
                            } catch (e: Exception) {
                                Log.e("LayoutParser", "Ошибка при парсинге property в properties", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        else -> {
                            // Пропускаем неизвестные теги
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return properties
    }

    /**
     * Парсит свойство из XML
     */
    private fun parseProperty(parser: XmlPullParser): EventProperty {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("LayoutParser", "      code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("LayoutParser", "      value: $value")
                        }
                        else -> {
                            // Пропускаем неизвестные теги
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return EventProperty(code, value)
    }

    /**
     * Пропускает текущий тег и все его дочерние элементы
     */
    private fun skipCurrentTag(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            return
        }
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    /**
     * Пропускает элементы до указанного закрывающего тега
     */
    private fun skipToEndTag(parser: XmlPullParser, tagName: String) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> {
                    depth--
                    if (parser.name == tagName && depth == 0) {
                        return
                    }
                }
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }
}