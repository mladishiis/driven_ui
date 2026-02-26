package com.example.drivenui.app.data

import com.example.drivenui.engine.parser.models.Component
import com.example.drivenui.engine.parser.models.LayoutComponent
import com.example.drivenui.engine.parser.models.WidgetComponent
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Gson TypeAdapter для sealed class Component (LayoutComponent | WidgetComponent).
 */
class ComponentTypeAdapter : JsonSerializer<Component>, JsonDeserializer<Component> {

    private val layoutKey = "layoutCode"
    private val widgetKey = "widgetCode"
    private val widgetTypeKey = "widgetType"

    override fun serialize(
        src: Component,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val json = when (src) {
            is LayoutComponent -> context.serialize(src, LayoutComponent::class.java)
            is WidgetComponent -> context.serialize(src, WidgetComponent::class.java)
        }.asJsonObject
        json.addProperty("_componentType", when (src) {
            is LayoutComponent -> "LayoutComponent"
            is WidgetComponent -> "WidgetComponent"
        })
        return json
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Component {
        val obj = json.asJsonObject
        val type = obj.remove("_componentType")?.takeIf { it.isJsonPrimitive }?.asString

        return when (type) {
            "LayoutComponent" -> context.deserialize<LayoutComponent>(obj, LayoutComponent::class.java)
            "WidgetComponent" -> context.deserialize<WidgetComponent>(obj, WidgetComponent::class.java)
            else -> when {
                obj.has(layoutKey) -> context.deserialize<LayoutComponent>(obj, LayoutComponent::class.java)
                obj.has(widgetKey) && obj.has(widgetTypeKey) ->
                    context.deserialize<WidgetComponent>(obj, WidgetComponent::class.java)
                else -> context.deserialize<LayoutComponent>(obj, LayoutComponent::class.java)
            }
        }
    }
}
