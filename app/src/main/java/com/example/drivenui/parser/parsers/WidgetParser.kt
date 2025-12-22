package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.EventProperty
import com.example.drivenui.parser.models.Widget
import com.example.drivenui.parser.models.WidgetEvent
import com.example.drivenui.parser.models.WidgetStyle
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class WidgetParser {

    fun parseWidgets(xmlContent: String): List<Widget> {
        Log.d("WidgetParser", "Начинаем парсинг виджетов")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val widgets = mutableListOf<Widget>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "widget" -> {
                            try {
                                val widget = parseWidget(parser)
                                if (widget.code.isNotEmpty() && widget.type.isNotEmpty()) {
                                    widgets.add(widget)
                                    Log.d("WidgetParser", "Успешно распарсен виджет: ${widget.title} (${widget.code})")
                                } else {
                                    Log.w("WidgetParser", "Пропущен виджет с пустыми кодом или типом: ${widget.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге widget", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("WidgetParser", "Найдено виджетов: ${widgets.size}")
        widgets.forEachIndexed { index, widget ->
            Log.d("WidgetParser", "  $index: ${widget.title} (${widget.code}) - ${widget.properties.size} свойств, ${widget.styles.size} стилей, ${widget.events.size} событий")
        }
        return widgets
    }

    private fun parseWidget(parser: XmlPullParser): Widget {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var type = ""
        val properties = mutableListOf<EventProperty>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()

        Log.d("WidgetParser", "Парсинг widget: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "widget")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("WidgetParser", "  code: $code")
                        }
                        "type" -> {
                            type = parser.nextText()
                            Log.d("WidgetParser", "  type: $type")
                        }
                        "properties" -> {
                            // Парсим вложенный блок properties
                            properties.addAll(parseProperties(parser))
                            Log.d("WidgetParser", "  properties: ${properties.size}")
                        }
                        "styles" -> {
                            // Парсим вложенный блок styles
                            styles.addAll(parseStyles(parser))
                            Log.d("WidgetParser", "  styles: ${styles.size}")
                        }
                        "events" -> {
                            // Парсим вложенный блок events
                            events.addAll(parseEvents(parser))
                            Log.d("WidgetParser", "  events: ${events.size}")
                        }
                        "property" -> {
                            // Если property напрямую внутри widget (не в блоке properties)
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("WidgetParser", "  property: ${property.code} = ${property.value}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге property", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        "style" -> {
                            // Если style напрямую внутри widget (не в блоке styles)
                            try {
                                val style = parseStyle(parser)
                                styles.add(style)
                                Log.d("WidgetParser", "  style: ${style.code} = ${style.value}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге style", e)
                                skipToEndTag(parser, "style")
                            }
                        }
                        "event" -> {
                            // Если event напрямую внутри widget (не в блоке events)
                            try {
                                val event = parseWidgetEvent(parser)
                                events.add(event)
                                Log.d("WidgetParser", "  event: ${event.eventCode}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге event", e)
                                skipToEndTag(parser, "event")
                            }
                        }
                        else -> {
                            // Пропускаем неизвестные теги
                            Log.d("WidgetParser", "Пропущен тег в widget: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Widget(title, code, type, properties, styles, events, emptyList())
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
                                Log.d("WidgetParser", "    property в properties: ${property.code}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге property в properties", e)
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
     * Парсит вложенные styles из блока <styles>
     */
    private fun parseStyles(parser: XmlPullParser): List<WidgetStyle> {
        val styles = mutableListOf<WidgetStyle>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "styles")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "style" -> {
                            try {
                                val style = parseStyle(parser)
                                styles.add(style)
                                Log.d("WidgetParser", "    style в styles: ${style.code}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге style в styles", e)
                                skipToEndTag(parser, "style")
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

        return styles
    }

    /**
     * Парсит вложенные events из блока <events>
     */
    private fun parseEvents(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> {
                            try {
                                val event = parseWidgetEvent(parser)
                                events.add(event)
                                Log.d("WidgetParser", "    event в events: ${event.eventCode}")
                            } catch (e: Exception) {
                                Log.e("WidgetParser", "Ошибка при парсинге event в events", e)
                                skipToEndTag(parser, "event")
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

        return events
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
                            Log.d("WidgetParser", "      code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("WidgetParser", "      value: $value")
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
     * Парсит стиль из XML
     */
    private fun parseStyle(parser: XmlPullParser): WidgetStyle {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "style")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("WidgetParser", "      code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("WidgetParser", "      value: $value")
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

        return WidgetStyle(code, value)
    }

    /**
     * Парсит событие виджета из XML
     */
    private fun parseWidgetEvent(parser: XmlPullParser): WidgetEvent {
        var eventCode = ""
        var order = 0

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "event")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event_code" -> {
                            eventCode = parser.nextText()
                            Log.d("WidgetParser", "      event_code: $eventCode")
                        }
                        "order" -> {
                            order = parser.nextText().toIntOrNull() ?: 0
                            Log.d("WidgetParser", "      order: $order")
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

        return WidgetEvent(eventCode, order, emptyList())
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