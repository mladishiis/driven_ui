package com.example.drivenui.engine.cache.serialization

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Gson TypeAdapter для sealed interface UiAction (полиморфная сериализация).
 */
class UiActionTypeAdapter : JsonSerializer<UiAction>, JsonDeserializer<UiAction> {

    private val typeKey = "_uiActionType"

    override fun serialize(src: UiAction, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val (typeName, json) = when (src) {
            is UiAction.OpenScreen -> "OpenScreen" to context.serialize(src).asJsonObject
            is UiAction.OpenBottomSheet -> "OpenBottomSheet" to context.serialize(src).asJsonObject
            is UiAction.RefreshScreen -> "RefreshScreen" to context.serialize(src).asJsonObject
            is UiAction.RefreshWidget -> "RefreshWidget" to context.serialize(src).asJsonObject
            is UiAction.RefreshLayout -> "RefreshLayout" to context.serialize(src).asJsonObject
            is UiAction.OpenDeeplink -> "OpenDeeplink" to context.serialize(src).asJsonObject
            is UiAction.ExecuteQuery -> "ExecuteQuery" to context.serialize(src).asJsonObject
            is UiAction.DataTransform -> "DataTransform" to context.serialize(src).asJsonObject
            is UiAction.SaveToContext -> "SaveToContext" to context.serialize(src).asJsonObject
            is UiAction.NativeCode -> "NativeCode" to context.serialize(src).asJsonObject
            is UiAction.Back -> "Back" to com.google.gson.JsonObject()
            is UiAction.Empty -> "Empty" to com.google.gson.JsonObject()
        }
        json.addProperty(typeKey, typeName)
        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UiAction {
        val obj = json.asJsonObject
        val type = obj.remove(typeKey)?.takeIf { it.isJsonPrimitive }?.asString

        return when (type) {
            "OpenScreen" -> context.deserialize<UiAction.OpenScreen>(obj, UiAction.OpenScreen::class.java)
            "OpenBottomSheet" -> context.deserialize<UiAction.OpenBottomSheet>(obj, UiAction.OpenBottomSheet::class.java)
            "RefreshScreen" -> context.deserialize<UiAction.RefreshScreen>(obj, UiAction.RefreshScreen::class.java)
            "RefreshWidget" -> context.deserialize<UiAction.RefreshWidget>(obj, UiAction.RefreshWidget::class.java)
            "RefreshLayout" -> context.deserialize<UiAction.RefreshLayout>(obj, UiAction.RefreshLayout::class.java)
            "OpenDeeplink" -> context.deserialize<UiAction.OpenDeeplink>(obj, UiAction.OpenDeeplink::class.java)
            "ExecuteQuery" -> context.deserialize<UiAction.ExecuteQuery>(obj, UiAction.ExecuteQuery::class.java)
            "DataTransform" -> context.deserialize<UiAction.DataTransform>(obj, UiAction.DataTransform::class.java)
            "SaveToContext" -> context.deserialize<UiAction.SaveToContext>(obj, UiAction.SaveToContext::class.java)
            "NativeCode" -> context.deserialize<UiAction.NativeCode>(obj, UiAction.NativeCode::class.java)
            "Back" -> UiAction.Back
            "Empty" -> UiAction.Empty
            else -> UiAction.Empty
        }
    }
}
