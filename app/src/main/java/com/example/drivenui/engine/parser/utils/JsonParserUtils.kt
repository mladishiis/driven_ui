package com.example.drivenui.engine.parser.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Парсит строку JSON в объект.
 *
 * @param content содержимое JSON-файла
 * @return корневой [JsonObject] или null, если строка пустая или корень не является объектом
 */
internal fun parseJsonObject(content: String): JsonObject? =
    content.takeIf { it.isNotBlank() }
        ?.let { JsonParser.parseString(it) }
        ?.asObjectOrNull()

/**
 * Безопасно приводит [JsonElement] к [JsonObject].
 *
 * @return объект JSON или null для отсутствующего значения, null-значения и не-объекта
 */
internal fun JsonElement?.asObjectOrNull(): JsonObject? =
    this?.takeIf { it.isJsonObject }?.asJsonObject

/**
 * Возвращает элементы массива или оборачивает одиночное значение в список.
 *
 * Удобно для полей JSON экрана, где один и тот же узел может прийти как объект
 * или как массив объектов.
 *
 * @return список элементов JSON
 */
internal fun JsonElement?.asArrayOrSingle(): List<JsonElement> = when {
    this == null || isJsonNull -> emptyList()
    isJsonArray -> asJsonArray.toList()
    else -> listOf(this)
}

/**
 * Читает строковое значение по первому найденному имени поля.
 *
 * @param names возможные имена поля в порядке приоритета
 * @param default значение, которое возвращается, если ни одно поле не найдено
 * @return обрезанная строка или [default]
 */
internal fun JsonObject.string(vararg names: String, default: String = ""): String {
    names.forEach { name ->
        val value = get(name) ?: return@forEach
        if (value.isJsonPrimitive) return value.asString.trim()
    }
    return default
}

/**
 * Читает целочисленное значение по первому найденному имени поля.
 *
 * @param names возможные имена поля в порядке приоритета
 * @param default значение, которое возвращается, если поле отсутствует или не парсится как число
 * @return число или [default]
 */
internal fun JsonObject.int(vararg names: String, default: Int = 0): Int =
    string(*names).toIntOrNull() ?: default

/**
 * Читает плоскую карту строковых свойств из вложенного JSON-объекта.
 *
 * @param name имя контейнера со свойствами
 * @return карта имя свойства -> строковое значение
 */
internal fun JsonObject.objectMap(name: String): Map<String, String> {
    val container = get(name).asObjectOrNull() ?: return emptyMap()
    return container.entrySet()
        .filter { (_, value) -> value.isJsonPrimitive }
        .associate { (key, value) -> key to value.asString.trim() }
}

private fun JsonArray.toList(): List<JsonElement> =
    (0 until size()).map { get(it) }
