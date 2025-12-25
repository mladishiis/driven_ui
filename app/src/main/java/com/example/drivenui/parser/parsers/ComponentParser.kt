package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.binding.BindingParser
import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ComponentParser {

    private val bindingParser = BindingParser()

    /**
     * Парсит экран и все его компоненты
     */
    fun parseScreenWithComponents(xmlContent: String): ParsedScreen? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(xmlContent.reader())

            var title = ""
            var screenCode = ""
            var screenShortCode = ""
            var deeplink = ""
            var rootComponent: Component? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "screen" -> {
                                title = parser.getAttributeValue(null, "title") ?: ""
                                eventType = parser.next()
                                while (!(eventType == XmlPullParser.END_TAG && parser.name == "screen")) {
                                    when (eventType) {
                                        XmlPullParser.START_TAG -> {
                                            when (parser.name) {
                                                "screenCode" -> screenCode = parser.nextText()
                                                "screenShortCode" -> screenShortCode = parser.nextText()
                                                "deeplink" -> deeplink = parser.nextText()
                                                "screenLayout" -> {
                                                    rootComponent = parseScreenLayoutComponent(parser)
                                                }
                                                else -> skipCurrentTag(parser)
                                            }
                                        }
                                    }
                                    eventType = parser.next()
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            if (screenCode.isNotEmpty()) {
                ParsedScreen(title, screenCode, screenShortCode, deeplink, rootComponent)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ComponentParser", "Ошибка парсинга экрана", e)
            null
        }
    }

    /**
     * Парсит компонент screenLayout
     */
    private fun parseScreenLayoutComponent(parser: XmlPullParser): Component? {
        return try {
            val title = parser.getAttributeValue(null, "title") ?: ""
            var screenLayoutCode = ""
            var layoutCode = ""
            var screenLayoutIndex = 0
            var forIndexName: String? = null
            val properties = mutableListOf<ComponentProperty>()  // Изменено
            val styles = mutableListOf<WidgetStyle>()
            val events = mutableListOf<WidgetEvent>()
            val children = mutableListOf<Component>()
            val bindingProperties = mutableListOf<String>()

            var eventType = parser.next()
            while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayout")) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "screenLayoutCode" -> screenLayoutCode = parser.nextText()
                            "layoutCode" -> layoutCode = parser.nextText()
                            "screenLayoutIndex" -> screenLayoutIndex = parser.nextText().toIntOrNull() ?: 0
                            "forIndexName" -> forIndexName = parser.nextText()
                            "properties" -> properties.addAll(parseProperties(parser))  // Новый метод
                            "property" -> parseComponentProperty(parser)?.let { properties.add(it) }  // Новый метод
                            "styles" -> styles.addAll(parseStyles(parser))
                            "style" -> parseStyle(parser)?.let { styles.add(it) }
                            "events" -> events.addAll(parseEvents(parser))
                            "event" -> parseEvent(parser)?.let { events.add(it) }
                            "screenLayout" -> {
                                parseScreenLayoutComponent(parser)?.let { children.add(it) }
                            }
                            "screenLayoutWidget" -> {
                                parseWidgetComponent(parser)?.let { children.add(it) }
                            }
                            "bindingProperties" -> {
                                bindingProperties.addAll(parseBindingProperties(parser))
                            }
                            else -> skipCurrentTag(parser)
                        }
                    }
                    XmlPullParser.TEXT -> {
                        // Обработка текста между тегами
                    }
                }
                eventType = parser.next()
            }

            LayoutComponent(
                title = title,
                code = screenLayoutCode,
                layoutCode = layoutCode,
                properties = properties,  // ComponentProperty
                styles = styles,
                events = events,
                children = children,
                bindingProperties = bindingProperties,
                index = screenLayoutIndex,
                forIndexName = forIndexName
            )
        } catch (e: Exception) {
            Log.e("ComponentParser", "Ошибка при парсинге screenLayout", e)
            null
        }
    }

    /**
     * Парсит компонент виджета
     */
    private fun parseWidgetComponent(parser: XmlPullParser): Component? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutWidgetCode = ""
        var widgetCode = ""
        val properties = mutableListOf<ComponentProperty>()  // Изменено
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val bindingProperties = mutableListOf<String>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screenLayoutWidget")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenLayoutWidgetCode" -> screenLayoutWidgetCode = parser.nextText()
                        "widgetCode" -> widgetCode = parser.nextText()
                        "properties" -> properties.addAll(parseProperties(parser))  // Новый метод
                        "property" -> parseComponentProperty(parser)?.let { properties.add(it) }  // Новый метод
                        "styles" -> styles.addAll(parseStyles(parser))
                        "style" -> parseStyle(parser)?.let { styles.add(it) }
                        "events" -> events.addAll(parseEvents(parser))
                        "event" -> parseEvent(parser)?.let { events.add(it) }
                        "bindingProperties" -> {
                            bindingProperties.addAll(parseBindingProperties(parser))
                        }
                        else -> skipCurrentTag(parser)
                    }
                }
                XmlPullParser.TEXT -> {
                    // Обработка текста между тегами
                }
            }
            eventType = parser.next()
        }

        val actualWidgetCode = when (widgetCode) {
            "label" -> {
                if (bindingProperties.any { it.contains("image") || it.contains(".svg") }) {
                    "image"
                } else {
                    widgetCode
                }
            }
            else -> widgetCode
        }

        return WidgetComponent(
            title = title,
            code = screenLayoutWidgetCode,
            widgetCode = actualWidgetCode,
            widgetType = "native",
            properties = properties,  // ComponentProperty
            styles = styles,
            events = events,
            bindingProperties = bindingProperties
        )
    }

    /**
     * Парсит свойства компонента (новый метод)
     */
    private fun parseProperties(parser: XmlPullParser): List<ComponentProperty> {
        val properties = mutableListOf<ComponentProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> parseComponentProperty(parser)?.let { properties.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return properties
    }

    /**
     * Парсит одно свойство компонента с извлечением макросов (новый метод)
     */
    private fun parseComponentProperty(parser: XmlPullParser): ComponentProperty? {
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

        return if (code.isNotEmpty()) {
            // Извлекаем макросы из значения
            val bindings = bindingParser.parseBindings(value)
            ComponentProperty(code, value, bindings)
        } else {
            null
        }
    }

    /**
     * Парсит EventProperty (для обратной совместимости)
     */
    private fun parseEventProperty(parser: XmlPullParser): EventProperty? {
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

        return if (code.isNotEmpty()) EventProperty(code, value) else null
    }

    // Остальные методы остаются без изменений...
    private fun parseStyles(parser: XmlPullParser): List<WidgetStyle> {
        val styles = mutableListOf<WidgetStyle>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "styles")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "style" -> parseStyle(parser)?.let { styles.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return styles
    }

    private fun parseStyle(parser: XmlPullParser): WidgetStyle? {
        var code = ""
        var value = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "style")) {
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

        return if (code.isNotEmpty()) WidgetStyle(code, value) else null
    }

    private fun parseEvents(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> parseEvent(parser)?.let { events.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return events
    }

    private fun parseEvent(parser: XmlPullParser): WidgetEvent? {
        var eventCode = ""
        var order = 0
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "event")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event_code" -> eventCode = parser.nextText()
                        "order" -> order = parser.nextText().toIntOrNull() ?: 0
                        "eventActions" -> eventActions.addAll(parseEventActions(parser))
                        "eventAction" -> parseEventAction(parser)?.let { eventActions.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return if (eventCode.isNotEmpty()) WidgetEvent(eventCode, order, eventActions) else null
    }

    private fun parseEventActions(parser: XmlPullParser): List<EventAction> {
        val eventActions = mutableListOf<EventAction>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventActions")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "eventAction" -> parseEventAction(parser)?.let { eventActions.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return eventActions
    }

    private fun parseEventAction(parser: XmlPullParser): EventAction? {
        var code = ""
        var order = 0
        val properties = mutableListOf<EventProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "eventAction")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "order" -> order = parser.nextText().toIntOrNull() ?: 0
                        "properties" -> properties.addAll(parseEventProperties(parser))
                        "property" -> parseEventProperty(parser)?.let { properties.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return if (code.isNotEmpty()) EventAction("", code, order, properties) else null
    }

    private fun parseEventProperties(parser: XmlPullParser): List<EventProperty> {
        val properties = mutableListOf<EventProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> parseEventProperty(parser)?.let { properties.add(it) }
                        else -> skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return properties
    }

    private fun parseBindingProperties(parser: XmlPullParser): List<String> {
        val bindingProperties = mutableListOf<String>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "bindingProperties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "value" -> bindingProperties.add(parser.nextText())
                        else -> skipCurrentTag(parser)
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        bindingProperties.add(text)
                    }
                }
            }
            eventType = parser.next()
        }

        return bindingProperties
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

    /**
     * Парсит все компоненты из полного XML микроаппа
     */
    fun parseAllComponentsFromFullXml(xmlContent: String): List<ParsedScreen> {
        val screens = mutableListOf<ParsedScreen>()

        val startTag = "<screens>"
        val endTag = "</screens>"

        val startIndex = xmlContent.indexOf(startTag)
        if (startIndex == -1) {
            Log.d("ComponentParser", "Блок <screens> не найден")
            return emptyList()
        }

        val endIndex = xmlContent.indexOf(endTag, startIndex)
        if (endIndex == -1) {
            Log.d("ComponentParser", "Закрывающий тег </screens> не найден")
            return emptyList()
        }

        val screensXml = xmlContent.substring(startIndex, endIndex + endTag.length)
        Log.d("ComponentParser", "Извлечен блок screens, размер: ${screensXml.length} символов")

        val screenPattern = "<screen[^>]*>.*?</screen>"
        val regex = Regex(screenPattern, RegexOption.DOT_MATCHES_ALL)

        val matches = regex.findAll(screensXml)
        matches.forEachIndexed { index, matchResult ->
            val screenXml = matchResult.value
            Log.d("ComponentParser", "Парсинг экрана $index, размер: ${screenXml.length} символов")
            try {
                parseScreenWithComponents(screenXml)?.let { screen ->
                    screens.add(screen)
                    Log.d("ComponentParser", "Экран добавлен: ${screen.title}")
                }
            } catch (e: Exception) {
                Log.e("ComponentParser", "Ошибка при парсинге экрана $index", e)
            }
        }

        Log.d("ComponentParser", "Найдено экранов: ${screens.size}")
        return screens
    }
}