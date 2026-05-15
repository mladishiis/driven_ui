package com.example.drivenui.engine.parser.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal fun parseJsonObject(content: String): JsonObject? =
    content.takeIf { it.isNotBlank() }
        ?.let { JsonParser.parseString(it) }
        ?.asObjectOrNull()

internal fun JsonElement?.asObjectOrNull(): JsonObject? =
    this?.takeIf { it.isJsonObject }?.asJsonObject

internal fun JsonElement?.asArrayOrSingle(): List<JsonElement> = when {
    this == null || isJsonNull -> emptyList()
    isJsonArray -> asJsonArray.toList()
    else -> listOf(this)
}

internal fun JsonObject.string(vararg names: String, default: String = ""): String {
    names.forEach { name ->
        val value = get(name) ?: return@forEach
        if (value.isJsonPrimitive) return value.asString.trim()
    }
    return default
}

internal fun JsonObject.int(vararg names: String, default: Int = 0): Int =
    string(*names).toIntOrNull() ?: default

internal fun JsonObject.objectMap(name: String): Map<String, String> {
    val container = get(name).asObjectOrNull() ?: return emptyMap()
    return container.entrySet()
        .filter { (_, value) -> value.isJsonPrimitive }
        .associate { (key, value) -> key to value.asString.trim() }
}

private fun JsonArray.toList(): List<JsonElement> =
    (0 until size()).map { get(it) }
