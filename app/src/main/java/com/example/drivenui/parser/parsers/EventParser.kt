package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер событий и действий из XML формата Driven UI
 */
class EventParser {

    /**
     * Парсит все события из XML строки
     */
    fun parseEvents(xmlContent: String): AllEvents {
        Log.d("EventParser", "Начинаем парсинг событий")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val events = mutableListOf<Event>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> {
                            try {
                                val event = parseEvent(parser)
                                if (event.code.isNotEmpty()) {
                                    events.add(event)
                                    Log.d("EventParser", "Успешно распарсен event: ${event.title} (${event.code})")
                                } else {
                                    Log.w("EventParser", "Пропущен event с пустым кодом: ${event.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("EventParser", "Ошибка при парсинге event", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("EventParser", "Найдено событий: ${events.size}")
        return AllEvents(events = events)
    }

    private fun parseEvent(parser: XmlPullParser): Event {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var order = 0
        val eventActions = mutableListOf<EventAction>()

        Log.d("EventParser", "Парсинг event: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "event")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("EventParser", "  code: $code")
                        }
                        "order" -> {
                            val orderText = parser.nextText()
                            order = orderText.toIntOrNull() ?: 0
                            Log.d("EventParser", "  order: $order")
                        }
                        "eventActions" -> {
                            eventActions.addAll(parseEventActionsBlock(parser))
                            Log.d("EventParser", "  eventActions: ${eventActions.size}")
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Event(title, code, order, eventActions)
    }

    private fun parseEventActionsBlock(parser: XmlPullParser): List<EventAction> {
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventActions")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventAction" -> {
                            try {
                                val eventAction = parseEventAction(parser)
                                if (eventAction.code.isNotEmpty()) {
                                    eventActions.add(eventAction)
                                    Log.d("EventParser", "    eventAction: ${eventAction.title} (${eventAction.code})")
                                }
                            } catch (e: Exception) {
                                Log.e("EventParser", "Ошибка при парсинге eventAction", e)
                                skipToEndTag(parser, "eventAction")
                            }
                        }
                        else -> {
                            Log.d("EventParser", "Пропущен тег в eventActions: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return eventActions
    }

    /**
     * Парсит все действия событий из XML строки
     */
    fun parseEventActions(xmlContent: String): AllEventActions {
        Log.d("EventParser", "Начинаем парсинг действий событий")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventAction" -> {
                            try {
                                val eventAction = parseEventAction(parser)
                                if (eventAction.code.isNotEmpty()) {
                                    eventActions.add(eventAction)
                                }
                            } catch (e: Exception) {
                                Log.e("EventParser", "Ошибка при парсинге eventAction в allEventActions", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("EventParser", "Найдено действий событий: ${eventActions.size}")
        return AllEventActions(eventActions)
    }

    private fun parseEventAction(parser: XmlPullParser): EventAction {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var order = 0
        val propertiesMap = mutableMapOf<String, String>()

        Log.d("EventParser", "Парсинг eventAction: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventAction")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                        }
                        "order" -> {
                            val orderText = parser.nextText()
                            order = orderText.toIntOrNull() ?: 0
                        }
                        "properties" -> {
                            // Парсим свойства в Map
                            parsePropertiesToMap(parser, propertiesMap)
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return EventAction(title, code, order, propertiesMap)
    }

    /**
     * Парсит свойства в Map
     */
    private fun parsePropertiesToMap(parser: XmlPullParser, map: MutableMap<String, String>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            try {
                                val property = parsePropertyToMap(parser)
                                if (property.first.isNotEmpty()) {
                                    map[property.first] = property.second
                                }
                            } catch (e: Exception) {
                                Log.e("EventParser", "Ошибка при парсинге property", e)
                                skipToEndTag(parser, "property")
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
    }

    /**
     * Парсит отдельное свойство в пару ключ-значение
     */
    private fun parsePropertyToMap(parser: XmlPullParser): Pair<String, String> {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                        }
                        "value" -> {
                            value = parser.nextText()
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Pair(code, value)
    }

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