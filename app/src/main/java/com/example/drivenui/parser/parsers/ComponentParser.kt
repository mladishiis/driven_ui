package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ComponentParser {

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
     * Парсит один экран
     */
    private fun parseScreen(parser: XmlPullParser): ParsedScreen? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenCode = ""
        var screenShortCode = ""
        var deeplink = ""
        var rootComponent: Component? = null

        Log.d("ComponentParser", "Парсинг экрана: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "screen")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "screenCode" -> {
                            screenCode = parser.nextText().trim()
                            Log.d("ComponentParser", "screenCode: $screenCode")
                        }
                        "screenShortCode" -> {
                            screenShortCode = parser.nextText().trim()
                        }
                        "deeplink" -> {
                            deeplink = parser.nextText().trim()
                        }
                        "screenLayout" -> {
                            rootComponent = parseScreenLayout(parser, 0)
                            if (rootComponent != null) {
                                Log.d("ComponentParser", "Корневой компонент создан: ${rootComponent.title}")
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

        return if (screenCode.isNotEmpty()) {
            ParsedScreen(title, screenCode, screenShortCode, deeplink, rootComponent)
        } else {
            null
        }
    }

    /**
     * Парсит screenLayout рекурсивно
     */
    private fun parseScreenLayout(parser: XmlPullParser, depth: Int): Component? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutCode = ""
        var layoutCode = ""
        var screenLayoutIndex = 0
        var maxForIndex: String? = null
        var forIndexName: String? = null
        val properties = mutableListOf<ComponentProperty>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val children = mutableListOf<Component>()
        val bindingProperties = mutableListOf<String>()

        Log.d("ComponentParser", "${"  ".repeat(depth)}Парсинг screenLayout: $title (глубина: $depth)")

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
                            properties.addAll(parsePropertiesBlock(parser))
                        }
                        "property" -> {
                            // Отдельное свойство без блока properties
                            parseComponentProperty(parser)?.let { properties.add(it) }
                        }
                        "styles" -> {
                            styles.addAll(parseStylesBlock(parser))
                        }
                        "style" -> {
                            // Отдельный стиль без блока styles
                            parseStyle(parser)?.let { styles.add(it) }
                        }
                        "events" -> {
                            events.addAll(parseEventsBlock(parser))
                        }
                        "screenLayout" -> {
                            // Рекурсивный вызов для вложенного layout
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
     * Парсит виджет внутри layout
     */
    private fun parseScreenLayoutWidget(parser: XmlPullParser, depth: Int): Component? {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var screenLayoutWidgetCode = ""
        var widgetCode = ""
        val properties = mutableListOf<ComponentProperty>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val bindingProperties = mutableListOf<String>()

        Log.d("ComponentParser", "${"  ".repeat(depth)}Парсинг widget: $title")

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
                            properties.addAll(parsePropertiesBlock(parser))
                        }
                        "property" -> {
                            parseComponentProperty(parser)?.let { properties.add(it) }
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

        // Определяем тип виджета на основе кода
        val widgetType = determineWidgetType(widgetCode)

        return if (screenLayoutWidgetCode.isNotEmpty() && widgetCode.isNotEmpty()) {
            WidgetComponent(
                title = title,
                code = screenLayoutWidgetCode,
                widgetCode = widgetCode,
                widgetType = widgetType,
                properties = properties,
                styles = styles,
                events = events, // События виджета
                bindingProperties = bindingProperties
            )
        } else {
            null
        }
    }

    /**
     * Определяет тип виджета
     */
    private fun determineWidgetType(widgetCode: String): String {
        return when (widgetCode) {
            "label", "button", "image", "checkbox", "switcher", "input", "appbar" -> "native"
            else -> "composite"
        }
    }

    /**
     * Парсит блок свойств
     */
    private fun parsePropertiesBlock(parser: XmlPullParser): List<ComponentProperty> {
        val properties = mutableListOf<ComponentProperty>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "property") {
                        parseComponentProperty(parser)?.let { properties.add(it) }
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
     * Парсит одно свойство с макросами
     */
    private fun parseComponentProperty(parser: XmlPullParser): ComponentProperty? {
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
            ComponentProperty(
                code = code,
                rawValue = value,
                resolvedValue = value // Пока без подстановки
            )
        } else {
            null
        }
    }

    /**
     * Парсит блок стилей
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
     * Парсит один стиль
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
     * Парсит блок событий - ИСПРАВЛЕННАЯ ВЕРСИЯ
     */
    private fun parseEventsBlock(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "event") {
                        // Здесь нужно сначала проверить структуру события
                        try {
                            val event = parseEventWithComplexStructure(parser)
                            if (event != null) {
                                events.add(event)
                            }
                        } catch (e: Exception) {
                            Log.e("ComponentParser", "Ошибка при парсинге события со сложной структурой", e)
                            // Попробуем простой вариант
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

        Log.d("ComponentParser", "Всего событий распарсено: ${events.size}")
        return events
    }

    /**
     * Парсит сложную структуру события (с тегами внутри)
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
                        "event_code" -> {
                            eventCode = parser.nextText().trim()
                            Log.d("ComponentParser", "Найден event_code: $eventCode")
                        }
                        "order" -> {
                            order = parser.nextText().trim().toIntOrNull() ?: 0
                        }
                        "eventActions" -> {
                            // Парсим блок eventActions
                            val actions = parseEventActionsBlock(parser)
                            eventActions.addAll(actions)
                            Log.d("ComponentParser", "Добавлено действий: ${actions.size}")
                        }
                        else -> {
                            // Если это не тег event_code, возможно это просто текст события
                            if (eventCode.isEmpty()) {
                                try {
                                    eventCode = parser.nextText().trim()
                                    Log.d("ComponentParser", "Найден текст события: $eventCode")
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
                            Log.d("ComponentParser", "Найден текст события из TEXT: $eventCode")
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
                eventActions = eventActions
            )
        } else {
            null
        }
    }

    /**
     * Парсит блок eventActions с содержащимися в нем eventAction элементами
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

        Log.d("ComponentParser", "Всего eventActions в блоке: ${eventActions.size}")
        return eventActions
    }

    /**
     * Парсит один eventAction элемент
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
                        "code" -> {
                            code = parser.nextText().trim()
                            Log.d("ComponentParser", "Найден код действия: $code")
                        }
                        "order" -> {
                            order = parser.nextText().trim().toIntOrNull() ?: 0
                        }
                        "properties" -> {
                            // Парсим блок свойств действия
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
                properties = properties
            )
        } else {
            null
        }
    }

    /**
     * Парсит свойства eventAction
     */
    private fun parseEventActionProperties(parser: XmlPullParser, properties: MutableMap<String, String>) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "property") {
                        parseEventActionProperty(parser)?.let { (key, value) ->
                            properties[key] = value
                            Log.d("ComponentParser", "        Добавлено свойство действия: $key = $value")
                        }
                    } else {
                        skipCurrentTag(parser)
                    }
                }
            }
            eventType = parser.next()
        }
        Log.d("ComponentParser", "        Всего свойств действия: ${properties.size}")
    }

    /**
     * Парсит одно свойство eventAction
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

    /**
     * Вспомогательная функция для отладки
     */
    fun logComponentTree(component: Component?, indent: String = "") {
        if (component == null) return

        when (component) {
            is LayoutComponent -> {
                Log.d("ComponentTree", "$indent[LAYOUT] ${component.title} (${component.code})")
                Log.d("ComponentTree", "$indent  layoutCode: ${component.layoutCode}")
                Log.d("ComponentTree", "$indent  children: ${component.children.size}")
                Log.d("ComponentTree", "$indent  событий: ${component.events.size}")

                component.events.forEach { event ->
                    Log.d("ComponentTree", "$indent    Событие: ${event.eventCode}, order: ${event.order}")
                    if (event.eventActions.isNotEmpty()) {
                        Log.d("ComponentTree", "$indent      Действий: ${event.eventActions.size}")
                        event.eventActions.forEach { action ->
                            Log.d("ComponentTree", "$indent        Действие: ${action.code}, order: ${action.order}")
                            if (action.properties.isNotEmpty()) {
                                Log.d("ComponentTree", "$indent          Свойства: ${action.properties.size}")
                                action.properties.forEach { (key, value) ->
                                    Log.d("ComponentTree", "$indent            $key = $value")
                                }
                            } else {
                                Log.d("ComponentTree", "$indent          Свойства: нет")
                            }
                        }
                    }
                }

                component.children.forEach { child ->
                    logComponentTree(child, "$indent  ")
                }
            }
            is WidgetComponent -> {
                Log.d("ComponentTree", "$indent[WIDGET] ${component.title} (${component.code})")
                Log.d("ComponentTree", "$indent  widgetCode: ${component.widgetCode}")
                Log.d("ComponentTree", "$indent  событий: ${component.events.size}")

                component.events.forEach { event ->
                    Log.d("ComponentTree", "$indent    Событие: ${event.eventCode}, order: ${event.order}")
                    if (event.eventActions.isNotEmpty()) {
                        Log.d("ComponentTree", "$indent      Действий: ${event.eventActions.size}")
                        event.eventActions.forEach { action ->
                            Log.d("ComponentTree", "$indent        Действие: ${action.code}, order: ${action.order}")
                            if (action.properties.isNotEmpty()) {
                                Log.d("ComponentTree", "$indent          Свойства: ${action.properties.size}")
                                action.properties.forEach { (key, value) ->
                                    Log.d("ComponentTree", "$indent            $key = $value")
                                }
                            } else {
                                Log.d("ComponentTree", "$indent          Свойства: нет")
                            }
                        }
                    }
                }
            }
        }
    }
}