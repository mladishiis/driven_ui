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
                            properties.addAll(parseProperties(parser))
                        }
                        "property" -> {
                            val property = parseProperty(parser)
                            // Добавляем только если код не пустой
                            if (property.code.isNotEmpty()) {
                                properties.add(property)
                            }
                        }
                        "events" -> {
                            events.addAll(parseEventsSimple(parser))
                        }
                        "event" -> {
                            val event = parseWidgetEventSimple(parser)
                            events.add(event)
                        }
                        else -> {
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
     * Парсит упрощенную структуру events (только тег event с текстом)
     */
    private fun parseEventsSimple(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> {
                            val eventCode = parser.nextText()
                            if (eventCode.isNotEmpty()) {
                                events.add(WidgetEvent(eventCode, 0, emptyList()))
                            }
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return events
    }

    private fun parseWidgetEventSimple(parser: XmlPullParser): WidgetEvent {
        val eventCode = parser.nextText()
        return WidgetEvent(eventCode, 0, emptyList())
    }

    private fun parseProperties(parser: XmlPullParser): List<EventProperty> {
        val properties = mutableListOf<EventProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            val property = parseProperty(parser)
                            if (property.code.isNotEmpty()) {
                                properties.add(property)
                            }
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return properties
    }

    private fun parseProperty(parser: XmlPullParser): EventProperty {
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

        return EventProperty(code, value)
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