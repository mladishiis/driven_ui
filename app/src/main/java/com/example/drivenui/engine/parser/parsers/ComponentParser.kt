package com.example.drivenui.engine.parser.parsers

import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.EventAction
import com.example.drivenui.engine.parser.models.LayoutComponent
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.parser.models.WidgetComponent
import com.example.drivenui.engine.parser.models.WidgetEvent
import com.example.drivenui.engine.parser.models.WidgetStyle
import com.example.drivenui.engine.parser.utils.asArrayOrSingle
import com.example.drivenui.engine.parser.utils.asObjectOrNull
import com.example.drivenui.engine.parser.utils.int
import com.example.drivenui.engine.parser.utils.objectMap
import com.example.drivenui.engine.parser.utils.parseJsonObject
import com.example.drivenui.engine.parser.utils.string
import com.google.gson.JsonObject

/**
 * Парсер экранов Driven UI canvas JSON.
 *
 * Контракт входного экрана:
 * - корень: type=SCREEN, params.screenCode/screenShortCode/deepLink
 * - контейнеры: type=LAYOUT, layoutType=vertical/horizontal/layers/...
 * - виджеты: type=LABEL/IMAGE/BUTTON/INPUT/APPBAR/...
 * - события: дочерние узлы type=EVENT с params.eventCode и params.actionType
 */
class ComponentParser {

    fun parseSingleScreenJson(jsonContent: String): ParsedScreen? {
        val root = parseJsonObject(jsonContent) ?: return null
        if (!root.string("type").equals("SCREEN", ignoreCase = true)) return null
        return parseScreen(root)
    }

    private fun parseScreen(json: JsonObject): ParsedScreen? {
        val params = json.params()
        val screenCode = params?.string("screenCode")?.takeIf { it.isNotEmpty() }
            ?: json.string("screenCode", "code", "id")
        if (screenCode.isEmpty()) return null

        return ParsedScreen(
            title = params?.string("screenName")?.takeIf { it.isNotEmpty() } ?: json.string("name", "title"),
            screenCode = screenCode,
            screenShortCode = params?.string("screenShortCode") ?: json.string("screenShortCode"),
            deeplink = params?.string("deepLink") ?: json.string("deeplink"),
            rootComponent = parseRootLayout(json),
            events = parseEvents(json),
        )
    }

    private fun parseRootLayout(json: JsonObject): Component =
        LayoutComponent(
            title = json.string("name", "title"),
            code = json.string("id", default = ROOT_CODE),
            layoutCode = json.string("layoutType", default = DEFAULT_LAYOUT_TYPE),
            properties = parseProperties(json),
            styles = parseStyles(json),
            events = parseEvents(json),
            children = parseChildren(json),
            bindingProperties = parseBindingProperties(json),
            forIndexName = json.params()?.string("forIndexName")?.takeIf { it.isNotEmpty() },
            maxForIndex = json.params()?.string("maxForIndex")?.takeIf { it.isNotEmpty() },
        )

    private fun parseComponent(json: JsonObject): Component? {
        val type = json.string("type")
        if (type.isEmpty() || type.equals(EVENT_TYPE, ignoreCase = true)) return null
        return if (type.equals(LAYOUT_TYPE, ignoreCase = true)) {
            parseLayout(json)
        } else {
            parseWidget(json, type)
        }
    }

    private fun parseLayout(json: JsonObject): Component =
        LayoutComponent(
            title = json.string("name", "title"),
            code = json.string("id", "code"),
            layoutCode = json.string("layoutType", default = DEFAULT_LAYOUT_TYPE),
            properties = parseProperties(json),
            styles = parseStyles(json),
            events = parseEvents(json),
            children = parseChildren(json),
            bindingProperties = parseBindingProperties(json),
            index = json.int("index"),
            forIndexName = json.params()?.string("forIndexName")?.takeIf { it.isNotEmpty() },
            maxForIndex = json.params()?.string("maxForIndex")?.takeIf { it.isNotEmpty() },
        )

    private fun parseWidget(json: JsonObject, type: String): Component {
        val widgetCode = type.lowercase()
        return WidgetComponent(
            title = json.string("name", "title"),
            code = json.string("id", "code"),
            widgetCode = widgetCode,
            widgetType = determineWidgetType(widgetCode),
            properties = parseProperties(json),
            styles = parseStyles(json),
            events = parseEvents(json),
            children = parseChildren(json),
            bindingProperties = parseBindingProperties(json),
        )
    }

    private fun parseChildren(json: JsonObject): List<Component> =
        json.get("children")
            .asArrayOrSingle()
            .mapNotNull { it.asObjectOrNull() }
            .mapNotNull(::parseComponent)

    private fun parseProperties(json: JsonObject): Map<String, String> =
        json.objectMap("properties")

    private fun parseStyles(json: JsonObject): List<WidgetStyle> =
        json.objectMap("styles")
            .map { (code, value) -> WidgetStyle(code, value) }

    private fun parseEvents(json: JsonObject): List<WidgetEvent> {
        val actionsByEventCode = linkedMapOf<String, MutableList<EventAction>>()
        json.get("children")
            .asArrayOrSingle()
            .mapNotNull { it.asObjectOrNull() }
            .filter { it.string("type").equals(EVENT_TYPE, ignoreCase = true) }
            .forEachIndexed { index, event ->
                val params = event.params()
                val eventCode = params?.string("eventCode")?.takeIf { it.isNotEmpty() }
                    ?: event.string("eventCode", "code")
                val actionType = params?.string("actionType")?.takeIf { it.isNotEmpty() }
                    ?: event.string("actionType", "code")
                if (eventCode.isEmpty() || actionType.isEmpty()) return@forEachIndexed

                val action = EventAction(
                    title = event.string("name", "title"),
                    code = actionType,
                    order = event.int("order", default = index),
                    properties = parseProperties(event),
                )
                actionsByEventCode.getOrPut(eventCode) { mutableListOf() }.add(action)
            }

        return actionsByEventCode.entries.mapIndexed { index, (eventCode, actions) ->
            WidgetEvent(
                eventCode = eventCode,
                order = index,
                eventActions = actions,
            )
        }
    }

    private fun parseBindingProperties(json: JsonObject): List<String> =
        json.get("bindingProperties")
            .asArrayOrSingle()
            .mapNotNull { item ->
                when {
                    item.isJsonPrimitive -> item.asString.trim()
                    item.isJsonObject -> item.asJsonObject.string("code", "value", "name")
                    else -> null
                }
            }
            .filter { it.isNotEmpty() }

    private fun JsonObject.params(): JsonObject? =
        get("params").asObjectOrNull()

    private fun determineWidgetType(widgetCode: String): String =
        when (widgetCode) {
            "label", "button", "image", "checkbox", "switcher", "input", "appbar" -> "native"
            else -> "composite"
        }

    private companion object {
        const val ROOT_CODE = "root"
        const val DEFAULT_LAYOUT_TYPE = "vertical"
        const val LAYOUT_TYPE = "LAYOUT"
        const val EVENT_TYPE = "EVENT"
    }
}
