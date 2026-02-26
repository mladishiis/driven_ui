package com.example.drivenui.engine.cache.serialization

import com.example.drivenui.engine.cache.CachedAppBarModel
import com.example.drivenui.engine.cache.CachedButtonModel
import com.example.drivenui.engine.cache.CachedComponentModel
import com.example.drivenui.engine.cache.CachedImageModel
import com.example.drivenui.engine.cache.CachedInputModel
import com.example.drivenui.engine.cache.CachedLabelModel
import com.example.drivenui.engine.cache.CachedLayoutModel
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Gson TypeAdapter для sealed interface CachedComponentModel (полиморфная сериализация).
 */
class CachedComponentTypeAdapter : JsonSerializer<CachedComponentModel>, JsonDeserializer<CachedComponentModel> {

    private val typeKey = "_cachedComponentType"

    override fun serialize(
        src: CachedComponentModel,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val json = when (src) {
            is CachedLayoutModel -> context.serialize(src, CachedLayoutModel::class.java)
            is CachedLabelModel -> context.serialize(src, CachedLabelModel::class.java)
            is CachedButtonModel -> context.serialize(src, CachedButtonModel::class.java)
            is CachedImageModel -> context.serialize(src, CachedImageModel::class.java)
            is CachedAppBarModel -> context.serialize(src, CachedAppBarModel::class.java)
            is CachedInputModel -> context.serialize(src, CachedInputModel::class.java)
        }.asJsonObject
        json.addProperty(typeKey, when (src) {
            is CachedLayoutModel -> "Layout"
            is CachedLabelModel -> "Label"
            is CachedButtonModel -> "Button"
            is CachedImageModel -> "Image"
            is CachedAppBarModel -> "AppBar"
            is CachedInputModel -> "Input"
        })
        return json
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CachedComponentModel {
        val obj = json.asJsonObject
        val type = obj.remove(typeKey)?.takeIf { it.isJsonPrimitive }?.asString

        return when (type) {
            "Layout" -> context.deserialize<CachedLayoutModel>(obj, CachedLayoutModel::class.java)
            "Label" -> context.deserialize<CachedLabelModel>(obj, CachedLabelModel::class.java)
            "Button" -> context.deserialize<CachedButtonModel>(obj, CachedButtonModel::class.java)
            "Image" -> context.deserialize<CachedImageModel>(obj, CachedImageModel::class.java)
            "AppBar" -> context.deserialize<CachedAppBarModel>(obj, CachedAppBarModel::class.java)
            "Input" -> context.deserialize<CachedInputModel>(obj, CachedInputModel::class.java)
            else -> throw IllegalStateException("Unknown CachedComponentModel type: $type")
        }
    }
}
