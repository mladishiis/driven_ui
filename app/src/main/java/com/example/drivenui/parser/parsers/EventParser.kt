package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер событий и действий из XML формата Driven UI
 *
 * Поддерживает парсинг событий (events) и действий (eventActions)
 */
class EventParser {

    /**
     * Парсит все события из XML строки
     *
     * @param xmlContent XML строка содержащая блок <allEvents>
     * @return [AllEvents] контейнер со всеми событиями
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
        events.forEachIndexed { index, event ->
            Log.d("EventParser", "  $index: ${event.title} (${event.code}) - ${event.eventActions.size} действий")
        }
        return AllEvents(events = events)
    }

    /**
     * Парсит отдельное событие
     */
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
                            Log.d("EventParser", "Пропущен тег в event: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Event(title, code, order, eventActions)
    }

    /**
     * Парсит блок eventActions внутри события
     */
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
     *
     * @param xmlContent XML строка содержащая блок <allEventActions>
     * @return [AllEventActions] контейнер со всеми действиями событий
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
                                    Log.d("EventParser", "Успешно распарсен eventAction: ${eventAction.title} (${eventAction.code})")
                                } else {
                                    Log.w("EventParser", "Пропущен eventAction с пустым кодом: ${eventAction.title}")
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
        eventActions.forEachIndexed { index, eventAction ->
            Log.d("EventParser", "  $index: ${eventAction.title} (${eventAction.code}) - ${eventAction.properties.size} свойств")
        }
        return AllEventActions(eventActions)
    }

    /**
     * Парсит отдельное действие события
     */
    private fun parseEventAction(parser: XmlPullParser): EventAction {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var order = 0
        val properties = mutableListOf<EventProperty>()

        Log.d("EventParser", "Парсинг eventAction: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventAction")) {
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
                        "properties" -> {
                            properties.addAll(parseProperties(parser))
                            Log.d("EventParser", "  properties: ${properties.size}")
                        }
                        else -> {
                            Log.d("EventParser", "Пропущен тег в eventAction: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return EventAction(title, code, order, properties)
    }

    /**
     * Парсит блок properties
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
                                Log.d("EventParser", "    property: ${property.code} = ${property.value}")
                            } catch (e: Exception) {
                                Log.e("EventParser", "Ошибка при парсинге property", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        else -> {
                            Log.d("EventParser", "Пропущен тег в properties: ${parser.name}")
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
     * Парсит отдельное свойство
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
                            Log.d("EventParser", "      code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("EventParser", "      value: $value")
                        }
                        else -> {
                            Log.d("EventParser", "Пропущен тег в property: ${parser.name}")
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