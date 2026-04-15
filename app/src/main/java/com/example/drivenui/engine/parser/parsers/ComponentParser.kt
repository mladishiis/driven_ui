package com.example.drivenui.engine.parser.parsers

import android.util.Log
import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.EventAction
import com.example.drivenui.engine.parser.models.LayoutComponent
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.parser.models.WidgetComponent
import com.example.drivenui.engine.parser.models.WidgetEvent
import com.example.drivenui.engine.parser.models.WidgetStyle
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер компонентов Driven UI из XML.
 * Парсит screenLayout, screenLayoutWidget, properties, styles и events.
 */
class ComponentParser {

    /**
     * Парсит один экран из XML-строки.
     *
     * @param xmlContent XML-строка с элементом screen
     * @return ParsedScreen при успешном парсинге или null
     */
    fun parseSingleScreenXml(xmlContent: String): ParsedScreen? {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "screen") {
                return parseScreen(parser)
            }
            eventType = parser.next()
        }
        return null
    }

    /**
     * Парсит один экран.
     *
     * @param parser XmlPullParser, позиционированный на теге screen
     * @return ParsedScreen при успешном парсинге или null
     */
    private fun parseScreen(parser: XmlPullParser): ParsedScreen? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenCode = ""
        var screenShortCode = ""
        var deeplink = ""
        var rootComponent: Component? = null
        val screenEvents = mutableListOf<WidgetEvent>()


        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screen")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenCode" -> {
                            screenCode = parser.nextText().trim()
                        }
                        "screenShortCode" -> {
                            screenShortCode = parser.nextText().trim()
                        }
                        "deeplink" -> {
                            deeplink = parser.nextText().trim()
                        }
                        "events" -> {
                            screenEvents.addAll(parseEventsBlock(parser))
                        }
                        "screenLayout" -> {
                            rootComponent = parseScreenLayout(parser, 0)
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return if (screenCode.isNotEmpty()) {
            ParsedScreen(
                title = title,
                screenCode = screenCode,
                screenShortCode = screenShortCode,
                deeplink = deeplink,
                rootComponent = rootComponent,
                events = screenEvents,
            )
        } else {
            null
        }
    }

    /**
     * Парсит screenLayout рекурсивно.
     *
     * @param parser XmlPullParser, позиционированный на теге screenLayout
     * @param depth Текущая глубина вложенности
     * @return LayoutComponent при успешном парсинге или null
     */
    private fun parseScreenLayout(parser: XmlPullParser, depth: Int): Component? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutCode = ""
        var layoutCode = ""
        var screenLayoutIndex = 0
        var maxForIndex: String? = null
        var forIndexName: String? = null
        val properties = mutableMapOf<String, String>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val children = mutableListOf<Component>()
        val bindingProperties = mutableListOf<String>()


        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayout")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayoutCode" -> {
                            screenLayoutCode = parser.nextText().trim()
                        }
                        "layoutCode" -> {
                            layoutCode = parser.nextText().trim()
                        }
                        "screenLayoutIndex" -> {
                            screenLayoutIndex = parser.nextText().trim().toIntOrNull() ?: 0
                        }
                        "forIndexName" -> {
                            forIndexName = parser.nextText().trim()
                        }
                        "maxForIndex" -> {
                            maxForIndex = parser.nextText().trim()
                        }
                        "properties" -> {
                            properties.putAll(parsePropertiesBlock(parser))
                        }
                        "property" -> {
                            parseProperty(parser)?.let { (code, value) -> properties[code] = value }
                        }
                        "styles" -> {
                            styles.addAll(parseStylesBlock(parser))
                        }
                        "style" -> {
                            parseStyle(parser)?.let { styles.add(it) }
                        }
                        "events" -> {
                            events.addAll(parseEventsBlock(parser))
                        }
                        "screenLayout" -> {
                            parseScreenLayout(parser, depth + 1)?.let { children.add(it) }
                        }
                        "screenLayoutWidget" -> {
                            parseScreenLayoutWidget(parser, depth + 1)?.let { children.add(it) }
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return if (screenLayoutCode.isNotEmpty()) {
            LayoutComponent(
                title = title,
                code = screenLayoutCode,
                layoutCode = layoutCode,
                properties = properties,
                styles = styles,
                events = events,
                children = children,
                bindingProperties = bindingProperties,
                index = screenLayoutIndex,
                forIndexName = forIndexName,
                maxForIndex = maxForIndex,
            )
        } else {
            null
        }
    }

    /**
     * Парсит виджет внутри layout.
     *
     * @param parser XmlPullParser, позиционированный на теге screenLayoutWidget
     * @param depth Текущая глубина вложенности
     * @return WidgetComponent при успешном парсинге или null
     */
    private fun parseScreenLayoutWidget(parser: XmlPullParser, depth: Int): Component? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutWidgetCode = ""
        var widgetCode = ""
        val properties = mutableMapOf<String, String>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val bindingProperties = mutableListOf<String>()


        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayoutWidget")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayoutWidgetCode" -> {
                            screenLayoutWidgetCode = parser.nextText().trim()
                        }
                        "widgetCode" -> {
                            widgetCode = parser.nextText().trim()
                        }
                        "properties" -> {
                            properties.putAll(parsePropertiesBlock(parser))
                        }
                        "property" -> {
                            parseProperty(parser)?.let { (code, value) -> properties[code] = value }
                        }
                        "styles" -> {
                            styles.addAll(parseStylesBlock(parser))
                        }
                        "style" -> {
                            parseStyle(parser)?.let { styles.add(it) }
                        }
                        "events" -> {
                            events.addAll(parseEventsBlock(parser))
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val widgetType = determineWidgetType(widgetCode)

        return if (screenLayoutWidgetCode.isNotEmpty() && widgetCode.isNotEmpty()) {
            WidgetComponent(
                title = title,
                code = screenLayoutWidgetCode,
                widgetCode = widgetCode,
                widgetType = widgetType,
                properties = properties,
                styles = styles,
                events = events,
                bindingProperties = bindingProperties,
            )
        } else {
            null
        }
    }

    /**
     * Определяет тип виджета.
     *
     * @param widgetCode Код виджета
     * @return "native" или "composite"
     */
    private fun determineWidgetType(widgetCode: String): String {
        return when (widgetCode) {
            "label", "button", "image", "checkbox", "switcher", "input", "appbar" -> "native"
            else -> "composite"
        }
    }

    /**
     * Парсит блок свойств.
     *
     * @param parser XmlPullParser, позиционированный на теге properties
     * @return Map код → значение
     */
    private fun parsePropertiesBlock(parser: XmlPullParser): Map<String, String> {
        val properties = mutableMapOf<String, String>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "property") {
                        parseProperty(parser)?.let { (code, value) -> properties[code] = value }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return properties
    }

    /**
     * Парсит одно свойство.
     *
     * @param parser XmlPullParser, позиционированный на теге property
     * @return Pair(code, value) или null
     */
    private fun parseProperty(parser: XmlPullParser): Pair<String, String>? {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText().trim()
                        "value" -> value = parser.nextText().trim()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return if (code.isNotEmpty()) code to value else null
    }

    /**
     * Парсит блок стилей.
     *
     * @param parser XmlPullParser, позиционированный на теге styles
     * @return Список WidgetStyle
     */
    private fun parseStylesBlock(parser: XmlPullParser): List<WidgetStyle> {
        val styles = mutableListOf<WidgetStyle>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "styles")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "style") {
                        parseStyle(parser)?.let { styles.add(it) }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return styles
    }

    /**
     * Парсит один стиль.
     *
     * @param parser XmlPullParser, позиционированный на теге style
     * @return WidgetStyle при успешном парсинге или null
     */
    private fun parseStyle(parser: XmlPullParser): WidgetStyle? {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "style")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText().trim()
                        "value" -> value = parser.nextText().trim()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return if (code.isNotEmpty() && value.isNotEmpty()) {
            WidgetStyle(code, value)
        } else {
            null
        }
    }

    /**
     * Парсит блок событий.
     *
     * @param parser XmlPullParser, позиционированный на теге events
     * @return Список WidgetEvent
     */
    private fun parseEventsBlock(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "event") {
                        try {
                            val event = parseEventWithComplexStructure(parser)
                            if (event != null) {
                                events.add(event)
                            }
                        } catch (e: Exception) {
                            Log.e("ComponentParser", "Ошибка при парсинге события со сложной структурой", e)
                            try {
                                val eventCode = parser.nextText().trim()
                                if (eventCode.isNotEmpty()) {
                                    events.add(WidgetEvent(eventCode = eventCode))
                                }
                            } catch (e2: Exception) {
                                Log.e("ComponentParser", "Ошибка при простом парсинге события", e2)
                                skipCurrentTag(parser)
                            }
                        }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return events
    }

    /**
     * Парсит сложную структуру события (с тегами внутри).
     *
     * @param parser XmlPullParser, позиционированный на теге event
     * @return WidgetEvent при успешном парсинге или null
     */
    private fun parseEventWithComplexStructure(parser: XmlPullParser): WidgetEvent? {
        var eventCode = ""
        var order = 0
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "event")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventСode" -> {
                            eventCode = parser.nextText().trim()
                        }
                        "order" -> {
                            order = parser.nextText().trim().toIntOrNull() ?: 0
                        }
                        "eventActions" -> {
                            val actions = parseEventActionsBlock(parser)
                            eventActions.addAll(actions)
                        }
                        else -> {
                            if (eventCode.isEmpty()) {
                                try {
                                    eventCode = parser.nextText().trim()
                                } catch (e: Exception) {
                                    Log.e("ComponentParser", "Ошибка чтения текста события", e)
                                }
                            } else {
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim()
                    if (text?.isNotEmpty() == true && !text.matches(Regex("\\s+"))) {
                        if (eventCode.isEmpty()) {
                            eventCode = text
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return if (eventCode.isNotEmpty()) {
            WidgetEvent(
                eventCode = eventCode,
                order = order,
                eventActions = eventActions,
            )
        } else {
            null
        }
    }

    /**
     * Парсит блок eventActions с содержащимися в нём eventAction элементами.
     *
     * @param parser XmlPullParser, позиционированный на теге eventActions
     * @return Список EventAction
     */
    private fun parseEventActionsBlock(parser: XmlPullParser): List<EventAction> {
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventActions")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "eventAction") {
                        parseEventAction(parser)?.let { eventActions.add(it) }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return eventActions
    }

    /**
     * Парсит один eventAction элемент.
     *
     * @param parser XmlPullParser, позиционированный на теге eventAction
     * @return EventAction при успешном парсинге или null
     */
    private fun parseEventAction(parser: XmlPullParser): EventAction? {
        var code = ""
        var order = 0
        val properties = mutableMapOf<String, String>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventAction")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventActionCode" -> {
                            code = parser.nextText().trim()
                        }
                        "order" -> {
                            order = parser.nextText().trim().toIntOrNull() ?: 0
                        }
                        "properties" -> {
                            parseEventActionProperties(parser, properties)
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return if (code.isNotEmpty()) {
            EventAction(
                code = code,
                order = order,
                properties = properties,
            )
        } else {
            null
        }
    }

    /**
     * Парсит свойства eventAction.
     *
     * @param parser XmlPullParser, позиционированный на теге properties
     * @param properties MutableMap для заполнения парами (code, value)
     */
    private fun parseEventActionProperties(parser: XmlPullParser, properties: MutableMap<String, String>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "property") {
                        parseEventActionProperty(parser)?.let { (key, value) ->
                            properties[key] = value
                        }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }
    }

    /**
     * Парсит одно свойство eventAction.
     *
     * @param parser XmlPullParser, позиционированный на теге property
     * @return Pair(code, value) при успешном парсинге или null
     */
    private fun parseEventActionProperty(parser: XmlPullParser): Pair<String, String>? {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "property")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText().trim()
                        "value" -> value = parser.nextText().trim()
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return if (code.isNotEmpty()) {
            Pair(code, value)
        } else {
            null
        }
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