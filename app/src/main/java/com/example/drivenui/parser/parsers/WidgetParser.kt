package com.example.drivenui.parser.parsers

import android.util.Log
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
                                if (widget.isValid()) {
                                    widgets.add(widget)
                                    Log.d("WidgetParser", "Успешно распарсен виджет: ${widget.title} (${widget.code})")
                                } else {
                                    Log.w("WidgetParser", "Пропущен невалидный виджет: ${widget.title}")
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
        val propertiesMap = mutableMapOf<String, String>()
        val styles = mutableListOf<WidgetStyle>()
        val events = mutableListOf<WidgetEvent>()
        val bindingProperties = mutableListOf<String>()

        Log.d("WidgetParser", "Парсинг widget: $title")

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "widget")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> {
                            code = parser.nextText().trim()
                        }
                        "type" -> {
                            type = parser.nextText().trim()
                        }
                        "properties" -> {
                            parsePropertiesToMap(parser, propertiesMap, bindingProperties)
                        }
                        "events" -> {
                            events.addAll(parseWidgetEvents(parser))
                        }
                        "styles" -> {
                            // Если в будущем добавятся стили для виджетов
                            styles.addAll(parseWidgetStyles(parser))
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Widget(
            title = title,
            code = code,
            type = type,
            properties = propertiesMap,
            styles = styles,
            events = events,
            bindingProperties = bindingProperties
        )
    }

    /**
     * Парсинг свойств с выявлением биндинг-свойств
     */
    private fun parsePropertiesToMap(
        parser: XmlPullParser,
        map: MutableMap<String, String>,
        bindingProperties: MutableList<String>
    ) {
        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "properties")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "property" -> {
                            val (propertyCode, propertyValue) = parsePropertyToPair(parser)
                            if (propertyCode.isNotEmpty()) {
                                map[propertyCode] = propertyValue

                                // Проверяем, является ли свойство биндинговым
                                if (isBindingProperty(propertyValue)) {
                                    bindingProperties.add(propertyCode)
                                    Log.d("WidgetParser", "Обнаружено биндинг-свойство: $propertyCode")
                                }
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
     * Определяет, содержит ли значение макросы для биндинга
     */
    private fun isBindingProperty(value: String): Boolean {
        return value.contains(Regex("""\$\{.+?\}"""))
    }

    private fun parsePropertyToPair(parser: XmlPullParser): Pair<String, String> {
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

        return Pair(code, value)
    }

    /**
     * Улучшенный парсинг событий виджета
     */
    private fun parseWidgetEvents(parser: XmlPullParser): List<WidgetEvent> {
        val events = mutableListOf<WidgetEvent>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "events")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "event" -> {
                            // Более сложная структура может быть в будущем
                            val eventCode = parser.nextText().trim()
                            if (eventCode.isNotEmpty()) {
                                val event = WidgetEvent(
                                    eventCode = eventCode,
                                    order = 0, // по умолчанию
                                    eventActions = emptyList() // TODO: парсить actions если будут
                                )
                                events.add(event)
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

    /**
     * Парсинг стилей виджета (если будут в будущем)
     */
    private fun parseWidgetStyles(parser: XmlPullParser): List<WidgetStyle> {
        val styles = mutableListOf<WidgetStyle>()

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "styles")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "style" -> {
                            val style = parseWidgetStyle(parser)
                            styles.add(style)
                        }
                        else -> {
                            skipCurrentTag(parser)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return styles
    }

    private fun parseWidgetStyle(parser: XmlPullParser): WidgetStyle {
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

        return WidgetStyle(code, value)
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
     * Вспомогательная функция для валидации виджета
     */
    private fun Widget.isValid(): Boolean {
        return code.isNotEmpty() &&
                type.isNotEmpty() &&
                (code != "unknown" || title.isNotEmpty())
    }
}