package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер экранов, лэйаутов и виджетов из XML формата Driven UI
 *
 * Поддерживает рекурсивный парсинг вложенных структур
 */
class ScreenParser {

    private var recursionDepth = 0
    private val MAX_RECURSION_DEPTH = 30 // Максимальная глубина рекурсии

    /**
     * Парсит все экраны из XML строки
     *
     * @param xmlContent XML строка содержащая блок <screens>
     * @return Список [Screen] объектов
     */
    fun parseScreens(xmlContent: String): List<Screen> {
        Log.d("ScreenParser", "Начинаем парсинг экранов")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val screens = mutableListOf<Screen>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screen" -> {
                            try {
                                val screen = parseScreen(parser)
                                if (screen.screenCode.isNotEmpty()) {
                                    screens.add(screen)
                                    Log.d("ScreenParser", "Успешно распарсен screen: ${screen.title} (${screen.screenCode})")
                                } else {
                                    Log.w("ScreenParser", "Пропущен screen с пустым кодом: ${screen.title}")
                                }
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге screen", e)
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        Log.d("ScreenParser", "Найдено экранов: ${screens.size}")
        screens.forEachIndexed { index, screen ->
            Log.d("ScreenParser", "  $index: ${screen.title} (${screen.screenCode}) - ${screen.screenLayouts.size} лэйаутов")
        }
        return screens
    }

    /**
     * Парсит отдельный экран из XML
     *
     * @param parser XmlPullParser позиционированный на теге <screen>
     * @return [Screen] объект экрана
     */
    private fun parseScreen(parser: XmlPullParser): Screen {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenCode = ""
        var screenShortCode = ""
        var deeplink = ""
        val properties = mutableListOf<EventProperty>()
        val events = mutableListOf<WidgetEvent>()
        val screenLayouts = mutableListOf<ScreenLayout>()

        Log.d("ScreenParser", "Парсинг screen: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screen")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenCode" -> {
                            screenCode = parser.nextText()
                            Log.d("ScreenParser", "  screenCode: $screenCode")
                        }
                        "screenShortCode" -> {
                            screenShortCode = parser.nextText()
                            Log.d("ScreenParser", "  screenShortCode: $screenShortCode")
                        }
                        "deeplink" -> {
                            deeplink = parser.nextText()
                            Log.d("ScreenParser", "  deeplink: $deeplink")
                        }
                        "properties" -> {
                            // Блок свойств (может быть пустой)
                            Log.d("ScreenParser", "  Найден блок properties")
                            skipCurrentTag(parser)
                        }
                        "property" -> {
                            // Если property напрямую внутри screen (не в блоке properties)
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("ScreenParser", "  property: ${property.code} = ${property.value}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге property", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        "events" -> {
                            events.addAll(parseEvents(parser))
                            Log.d("ScreenParser", "  events: ${events.size}")
                        }
                        "screenLayouts" -> {
                            screenLayouts.addAll(parseScreenLayouts(parser))
                            Log.d("ScreenParser", "  screenLayouts: ${screenLayouts.size}")
                        }
                        "screenLayout" -> {
                            // Если screenLayout напрямую внутри screen (не в блоке screenLayouts)
                            try {
                                val screenLayout = parseScreenLayout(parser)
                                screenLayouts.add(screenLayout)
                                Log.d("ScreenParser", "  screenLayout: ${screenLayout.title} (${screenLayout.screenLayoutCode})")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге screenLayout", e)
                                skipToEndTag(parser, "screenLayout")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в screen: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Screen(
            title,
            screenCode,
            screenShortCode,
            deeplink,
            properties,
            events,
            screenLayouts
        )
    }

    /**
     * Парсит блок events внутри экрана
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
                                val widgetEvent = parseWidgetEvent(parser)
                                events.add(widgetEvent)
                                Log.d("ScreenParser", "    event: ${widgetEvent.eventCode}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге event", e)
                                skipToEndTag(parser, "event")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в events: ${parser.name}")
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
     * Парсит блок screenLayouts внутри экрана
     */
    private fun parseScreenLayouts(parser: XmlPullParser): List<ScreenLayout> {
        val screenLayouts = mutableListOf<ScreenLayout>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayouts")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayout" -> {
                            try {
                                val screenLayout = parseScreenLayout(parser)
                                screenLayouts.add(screenLayout)
                                Log.d("ScreenParser", "    screenLayout: ${screenLayout.title}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге screenLayout в screenLayouts", e)
                                skipToEndTag(parser, "screenLayout")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в screenLayouts: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return screenLayouts
    }

    /**
     * Парсит отдельный screenLayout из XML (рекурсивно)
     *
     * @param parser XmlPullParser позиционированный на теге <screenLayout>
     * @return [ScreenLayout] объект лэйаута
     */
    private fun parseScreenLayout(parser: XmlPullParser): ScreenLayout {
        recursionDepth++

        // Защита от бесконечной рекурсии
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            throw IllegalStateException("Превышена максимальная глубина рекурсии ($MAX_RECURSION_DEPTH)")
        }

        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutCode = ""
        var layoutCode = ""
        var screenLayoutIndex = 0
        var forIndexName: String? = null
        val properties = mutableListOf<EventProperty>()
        val styles = mutableListOf<WidgetStyle>()
        val children = mutableListOf<ScreenLayout>()
        val widgets = mutableListOf<ScreenLayoutWidget>()

        Log.d("ScreenParser", "Парсинг screenLayout: $title (глубина: $recursionDepth)")

        try {
            var eventType = parser.next()
            while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayout")) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "screenLayoutCode" -> {
                                screenLayoutCode = parser.nextText()
                                Log.d("ScreenParser", "    screenLayoutCode: $screenLayoutCode")
                            }
                            "layoutCode" -> {
                                layoutCode = parser.nextText()
                                Log.d("ScreenParser", "    layoutCode: $layoutCode")
                            }
                            "screenLayoutIndex" -> {
                                val indexText = parser.nextText()
                                screenLayoutIndex = indexText.toIntOrNull() ?: 0
                                Log.d("ScreenParser", "    screenLayoutIndex: $screenLayoutIndex")
                            }
                            "forIndexName" -> {
                                forIndexName = parser.nextText()
                                Log.d("ScreenParser", "    forIndexName: $forIndexName")
                            }
                            "properties" -> {
                                properties.addAll(parseProperties(parser))
                                Log.d("ScreenParser", "    properties: ${properties.size}")
                            }
                            "property" -> {
                                // Если property напрямую внутри screenLayout
                                try {
                                    val property = parseProperty(parser)
                                    properties.add(property)
                                    Log.d("ScreenParser", "    property: ${property.code} = ${property.value}")
                                } catch (e: Exception) {
                                    Log.e("ScreenParser", "Ошибка при парсинге property", e)
                                    skipToEndTag(parser, "property")
                                }
                            }
                            "styles" -> {
                                styles.addAll(parseStyles(parser))
                                Log.d("ScreenParser", "    styles: ${styles.size}")
                            }
                            "style" -> {
                                // Если style напрямую внутри screenLayout
                                try {
                                    val style = parseStyle(parser)
                                    styles.add(style)
                                    Log.d("ScreenParser", "    style: ${style.code} = ${style.value}")
                                } catch (e: Exception) {
                                    Log.e("ScreenParser", "Ошибка при парсинге style", e)
                                    skipToEndTag(parser, "style")
                                }
                            }
                            "screenLayouts" -> {
                                children.addAll(parseChildScreenLayouts(parser))
                                Log.d("ScreenParser", "    children: ${children.size}")
                            }
                            "screenLayout" -> {
                                // Если screenLayout напрямую внутри screenLayout
                                try {
                                    val childScreenLayout = parseScreenLayout(parser)
                                    children.add(childScreenLayout)
                                    Log.d("ScreenParser", "    child screenLayout: ${childScreenLayout.title}")
                                } catch (e: Exception) {
                                    Log.e("ScreenParser", "Ошибка при парсинге child screenLayout", e)
                                    skipToEndTag(parser, "screenLayout")
                                }
                            }
                            "screenLayoutWidgets" -> {
                                widgets.addAll(parseScreenLayoutWidgets(parser))
                                Log.d("ScreenParser", "    widgets: ${widgets.size}")
                            }
                            "screenLayoutWidget" -> {
                                // Если screenLayoutWidget напрямую внутри screenLayout
                                try {
                                    val widget = parseScreenLayoutWidget(parser)
                                    widgets.add(widget)
                                    Log.d("ScreenParser", "    widget: ${widget.title}")
                                } catch (e: Exception) {
                                    Log.e("ScreenParser", "Ошибка при парсинге screenLayoutWidget", e)
                                    skipToEndTag(parser, "screenLayoutWidget")
                                }
                            }
                            else -> {
                                Log.d("ScreenParser", "Пропущен тег в screenLayout: ${parser.name}")
                                skipCurrentTag(parser)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            return ScreenLayout(
                title,
                screenLayoutCode,
                layoutCode,
                screenLayoutIndex,
                forIndexName,
                properties,
                styles,
                children,
                widgets
            )
        } finally {
            recursionDepth--
        }
    }

    /**
     * Парсит блок properties внутри screenLayout
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
                                Log.d("ScreenParser", "      property: ${property.code}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге property в properties", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в properties: ${parser.name}")
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
     * Парсит блок styles внутри screenLayout
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
                                Log.d("ScreenParser", "      style: ${style.code}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге style в styles", e)
                                skipToEndTag(parser, "style")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в styles: ${parser.name}")
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
     * Парсит блок child screenLayouts внутри screenLayout
     */
    private fun parseChildScreenLayouts(parser: XmlPullParser): List<ScreenLayout> {
        val children = mutableListOf<ScreenLayout>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayouts")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayout" -> {
                            try {
                                val child = parseScreenLayout(parser)
                                children.add(child)
                                Log.d("ScreenParser", "      child layout: ${child.title}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге child screenLayout", e)
                                skipToEndTag(parser, "screenLayout")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в child screenLayouts: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return children
    }

    /**
     * Парсит блок screenLayoutWidgets внутри screenLayout
     */
    private fun parseScreenLayoutWidgets(parser: XmlPullParser): List<ScreenLayoutWidget> {
        val widgets = mutableListOf<ScreenLayoutWidget>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayoutWidgets")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayoutWidget" -> {
                            try {
                                val widget = parseScreenLayoutWidget(parser)
                                widgets.add(widget)
                                Log.d("ScreenParser", "      widget: ${widget.title}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге screenLayoutWidget", e)
                                skipToEndTag(parser, "screenLayoutWidget")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в screenLayoutWidgets: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return widgets
    }

    /**
     * Парсит виджет в лэйауте из XML
     *
     * @param parser XmlPullParser позиционированный на теге <screenLayoutWidget>
     * @return [ScreenLayoutWidget] объект виджета
     */
    private fun parseScreenLayoutWidget(parser: XmlPullParser): ScreenLayoutWidget {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutWidgetCode = ""
        var widgetCode = ""
        val properties = mutableListOf<EventProperty>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val bindingProperties = mutableListOf<String>()

        Log.d("ScreenParser", "Парсинг screenLayoutWidget: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayoutWidget")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayoutWidgetCode" -> {
                            screenLayoutWidgetCode = parser.nextText()
                            Log.d("ScreenParser", "      screenLayoutWidgetCode: $screenLayoutWidgetCode")
                        }
                        "widgetCode" -> {
                            widgetCode = parser.nextText()
                            Log.d("ScreenParser", "      widgetCode: $widgetCode")
                        }
                        "properties" -> {
                            properties.addAll(parseWidgetProperties(parser))
                            Log.d("ScreenParser", "      properties: ${properties.size}")
                        }
                        "property" -> {
                            // Если property напрямую внутри widget
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("ScreenParser", "      property: ${property.code} = ${property.value}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге property в widget", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        "styles" -> {
                            styles.addAll(parseWidgetStyles(parser))
                            Log.d("ScreenParser", "      styles: ${styles.size}")
                        }
                        "style" -> {
                            // Если style напрямую внутри widget
                            try {
                                val style = parseStyle(parser)
                                styles.add(style)
                                Log.d("ScreenParser", "      style: ${style.code} = ${style.value}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге style в widget", e)
                                skipToEndTag(parser, "style")
                            }
                        }
                        "events" -> {
                            events.addAll(parseWidgetEvents(parser))
                            Log.d("ScreenParser", "      events: ${events.size}")
                        }
                        "event" -> {
                            // Если event напрямую внутри widget
                            try {
                                val widgetEvent = parseWidgetEvent(parser)
                                events.add(widgetEvent)
                                Log.d("ScreenParser", "      event: ${widgetEvent.eventCode}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге event в widget", e)
                                skipToEndTag(parser, "event")
                            }
                        }
                        "value" -> {
                            val value = parser.nextText()
                            bindingProperties.add(value)
                            Log.d("ScreenParser", "      binding value: $value")
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в screenLayoutWidget: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return ScreenLayoutWidget(
            title,
            screenLayoutWidgetCode,
            widgetCode,
            properties,
            styles,
            events,
            bindingProperties
        )
    }

    /**
     * Парсит блок properties внутри виджета
     */
    private fun parseWidgetProperties(parser: XmlPullParser): List<EventProperty> {
        return parseProperties(parser) // Используем тот же метод
    }

    /**
     * Парсит блок styles внутри виджета
     */
    private fun parseWidgetStyles(parser: XmlPullParser): List<WidgetStyle> {
        return parseStyles(parser) // Используем тот же метод
    }

    /**
     * Парсит блок events внутри виджета
     */
    private fun parseWidgetEvents(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> {
                            try {
                                val widgetEvent = parseWidgetEvent(parser)
                                events.add(widgetEvent)
                                Log.d("ScreenParser", "        event: ${widgetEvent.eventCode}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге event в events", e)
                                skipToEndTag(parser, "event")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в widget events: ${parser.name}")
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
     * Парсит событие виджета из XML
     *
     * @param parser XmlPullParser позиционированный на теге <event> внутри виджета
     * @return [WidgetEvent] объект события виджета
     */
    private fun parseWidgetEvent(parser: XmlPullParser): WidgetEvent {
        var eventCode = ""
        var order = 0
        val eventActions = mutableListOf<EventAction>()

        Log.d("ScreenParser", "Парсинг widget event")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "event")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            eventCode = parser.nextText()
                            Log.d("ScreenParser", "          eventCode: $eventCode")
                        }
                        "order" -> {
                            val orderText = parser.nextText()
                            order = orderText.toIntOrNull() ?: 0
                            Log.d("ScreenParser", "          order: $order")
                        }
                        "eventActions" -> {
                            eventActions.addAll(parseEventActions(parser))
                            Log.d("ScreenParser", "          eventActions: ${eventActions.size}")
                        }
                        "eventAction" -> {
                            // Если eventAction напрямую внутри event
                            try {
                                val eventAction = parseEventAction(parser)
                                eventActions.add(eventAction)
                                Log.d("ScreenParser", "          eventAction: ${eventAction.code}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге eventAction", e)
                                skipToEndTag(parser, "eventAction")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в widget event: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return WidgetEvent(eventCode, order, eventActions)
    }

    /**
     * Парсит блок eventActions внутри события
     */
    private fun parseEventActions(parser: XmlPullParser): List<EventAction> {
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventActions")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventAction" -> {
                            try {
                                val eventAction = parseEventAction(parser)
                                eventActions.add(eventAction)
                                Log.d("ScreenParser", "            eventAction: ${eventAction.code}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге eventAction в eventActions", e)
                                skipToEndTag(parser, "eventAction")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в eventActions: ${parser.name}")
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
     * Парсит действие события для виджета
     */
    private fun parseEventAction(parser: XmlPullParser): EventAction {
        var code = ""
        var order = 0
        val properties = mutableListOf<EventProperty>()

        Log.d("ScreenParser", "Парсинг eventAction")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventAction")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText()
                            Log.d("ScreenParser", "              code: $code")
                        }
                        "order" -> {
                            val orderText = parser.nextText()
                            order = orderText.toIntOrNull() ?: 0
                            Log.d("ScreenParser", "              order: $order")
                        }
                        "properties" -> {
                            properties.addAll(parseActionProperties(parser))
                            Log.d("ScreenParser", "              properties: ${properties.size}")
                        }
                        "property" -> {
                            // Если property напрямую внутри eventAction
                            try {
                                val property = parseProperty(parser)
                                properties.add(property)
                                Log.d("ScreenParser", "              property: ${property.code}")
                            } catch (e: Exception) {
                                Log.e("ScreenParser", "Ошибка при парсинге property в eventAction", e)
                                skipToEndTag(parser, "property")
                            }
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в eventAction: ${parser.name}")
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return EventAction("", code, order, properties)  // Пустой title для виджетов
    }

    /**
     * Парсит блок properties внутри действия события
     */
    private fun parseActionProperties(parser: XmlPullParser): List<EventProperty> {
        return parseProperties(parser) // Используем тот же метод
    }

    /**
     * Парсит свойство из XML
     *
     * @param parser XmlPullParser позиционированный на теге <property>
     * @return [EventProperty] объект свойства
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
                            Log.d("ScreenParser", "                  code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("ScreenParser", "                  value: $value")
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в property: ${parser.name}")
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
     *
     * @param parser XmlPullParser позиционированный на теге <style>
     * @return [WidgetStyle] объект стиля
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
                            Log.d("ScreenParser", "                  code: $code")
                        }
                        "value" -> {
                            value = parser.nextText()
                            Log.d("ScreenParser", "                  value: $value")
                        }
                        else -> {
                            Log.d("ScreenParser", "Пропущен тег в style: ${parser.name}")
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