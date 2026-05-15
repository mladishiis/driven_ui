package com.example.drivenui.engine.parser.parsers

import com.example.drivenui.engine.parser.models.Microapp
import com.example.drivenui.engine.parser.utils.asArrayOrSingle
import com.example.drivenui.engine.parser.utils.parseJsonObject
import com.example.drivenui.engine.parser.utils.string

/**
 * Парсер метаданных микроаппа из canvas JSON.
 */
class MicroappParser {

    /**
     * Ожидаемый контракт:
     * {
     *   "code": "...",
     *   "shortCode": "...",
     *   "deeplink": "...",
     *   "title": "...",
     *   "persistent": ["..."],
     *   "screen": [{ "code": "...", "title": "..." }]
     * }
     */
    fun parseMicroapp(jsonContent: String): Microapp? {
        val root = parseJsonObject(jsonContent) ?: return null
        return Microapp(
            title = root.string("title"),
            code = root.string("code"),
            shortCode = root.string("shortCode"),
            deeplink = root.string("deeplink"),
            persistents = root.get("persistent")
                .asArrayOrSingle()
                .mapNotNull { item ->
                    when {
                        item.isJsonPrimitive -> item.asString.trim()
                        item.isJsonObject -> item.asJsonObject.string("value", "code", "name")
                        else -> null
                    }
                }
                .filter { it.isNotEmpty() },
        )
    }
}
