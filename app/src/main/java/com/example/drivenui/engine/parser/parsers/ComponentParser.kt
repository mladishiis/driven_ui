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
 * Парсер экранов из JSON-описания.
 *
 * Контракт входного экрана:
 * - корень JSON: type=SCREEN → [ParsedScreen] (params, первый LAYOUT в children, events)
 * - контейнеры: type=LAYOUT, layoutType=vertical/horizontal/layers/...
 * - виджеты: type=LABEL/IMAGE/BUTTON/INPUT/APPBAR/...
 * - события: дочерние узлы type=EVENT с params.eventCode и params.actionType
 */
class ComponentParser {

    /**
     * Парсит один экран из JSON-файла (корень type=SCREEN).
     *
     * @param jsonContent JSON-строка с корнем type=SCREEN
     * @return [ParsedScreen] при успешном парсинге или null для пустого/не экранного JSON
     */
    fun parseSingleScreenJson(jsonContent: String): ParsedScreen? {
        val root = parseJsonObject(jsonContent) ?: return null
        if (!root.string("type").equals("SCREEN", ignoreCase = true)) return null
        return parseScreen(root)
    }

    /**
     * Собирает модель экрана из корневого JSON-объекта.
     *
     * @param json корневой объект экрана
     * @return [ParsedScreen] или null, если у экрана нет кода
     */
    private fun parseScreen(json: JsonObject): ParsedScreen? {
        val params = json.params()
        val screenCode = params?.string("screenCode")?.takeIf { it.isNotEmpty() }
            ?: json.string("screenCode", "code", "id")
        if (screenCode.isEmpty()) return null

        return ParsedScreen(
            title = params?.string("screenName").orEmpty(),
            screenCode = screenCode,
            screenShortCode = params?.string("screenShortCode").orEmpty(),
            deeplink = params?.string("deepLink").orEmpty(),
            rootComponent = parseFirstLayoutChild(json),
            events = parseEvents(json),
        )
    }

    /**
     * Возвращает первый LAYOUT среди children screen (EVENT и виджеты пропускаются).
     */
    private fun parseFirstLayoutChild(json: JsonObject): Component? =
        json.get("children")
            .asArrayOrSingle()
            .mapNotNull { it.asObjectOrNull() }
            .firstOrNull { it.string("type").equals(LAYOUT_TYPE, ignoreCase = true) }
            ?.let { parseLayout(it) }

    /**
     * Парсит дочерний UI-узел, пропуская EVENT-узлы.
     *
     * @param json объект дочернего узла
     * @return лэйаут или виджет, либо null для служебных узлов
     */
    private fun parseComponent(json: JsonObject): Component? {
        val type = json.string("type")
        if (type.isEmpty() || type.equals(EVENT_TYPE, ignoreCase = true)) return null
        return if (type.equals(LAYOUT_TYPE, ignoreCase = true)) {
            parseLayout(json)
        } else {
            parseWidget(json, type)
        }
    }

    /**
     * Парсит контейнерный LAYOUT-узел.
     *
     * @param json объект лэйаута
     * @return [LayoutComponent] с рекурсивно распарсенными дочерними компонентами
     */
    private fun parseLayout(json: JsonObject): Component =
        LayoutComponent(
            title = json.string("name", "title"),
            code = json.string("name", "code"),
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

    /**
     * Парсит виджетовый узел.
     *
     * @param json объект виджета
     * @param type значение поля type из JSON узла (LABEL, LAYOUT, …)
     * @return [WidgetComponent] с параметрами, стилями и событиями
     */
    private fun parseWidget(json: JsonObject, type: String): Component {
        val widgetCode = type.lowercase()
        return WidgetComponent(
            title = json.string("name", "title"),
            code = json.string("name", "code"),
            widgetCode = widgetCode,
            widgetType = determineWidgetType(widgetCode),
            properties = parseProperties(json),
            styles = parseStyles(json),
            events = parseEvents(json),
            children = parseChildren(json),
            bindingProperties = parseBindingProperties(json),
        )
    }

    /**
     * Парсит дочерние UI-компоненты текущего узла.
     *
     * @param json объект с полем children
     * @return список дочерних компонентов без EVENT-узлов
     */
    private fun parseChildren(json: JsonObject): List<Component> =
        json.get("children")
            .asArrayOrSingle()
            .mapNotNull { it.asObjectOrNull() }
            .mapNotNull(::parseComponent)

    /**
     * Парсит свойства компонента.
     *
     * @param json объект компонента
     * @return карта код свойства -> значение
     */
    private fun parseProperties(json: JsonObject): Map<String, String> =
        json.objectMap("properties")

    /**
     * Парсит стили компонента.
     *
     * @param json объект компонента
     * @return список стилей виджета или лэйаута
     */
    private fun parseStyles(json: JsonObject): List<WidgetStyle> =
        json.objectMap("styles")
            .map { (code, value) -> WidgetStyle(code, value) }

    /**
     * Парсит EVENT-узлы из children текущего компонента.
     *
     * @param json объект компонента
     * @return события, сгруппированные по eventCode
     */
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
                    queryString = event.objectMap("queryString"),
                    queryBody = event.objectMap("queryBody"),
                    queryHeader = event.objectMap("queryHeader"),
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

    /**
     * Парсит список свойств, значения которых должны заполняться биндингом.
     *
     * @param json объект компонента
     * @return список кодов bindingProperties
     */
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

    /**
     * Определяет тип виджета по его коду.
     *
     * @param widgetCode код виджета в нижнем регистре
     * @return "native" для поддержанных нативных компонентов, иначе "composite"
     */
    private fun determineWidgetType(widgetCode: String): String =
        when (widgetCode) {
            "label", "button", "image", "checkbox", "switcher", "input", "appbar" -> "native"
            else -> "composite"
        }

    private companion object {
        const val DEFAULT_LAYOUT_TYPE = "vertical"
        const val LAYOUT_TYPE = "LAYOUT"
        const val EVENT_TYPE = "EVENT"
    }
}
