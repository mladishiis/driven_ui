package com.example.drivenui.engine.parser.parsers

import com.example.drivenui.engine.parser.models.AlignmentStyle
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ColorStyle
import com.example.drivenui.engine.parser.models.ColorTheme
import com.example.drivenui.engine.parser.models.PaddingStyle
import com.example.drivenui.engine.parser.models.RoundStyle
import com.example.drivenui.engine.parser.models.TextStyle
import com.example.drivenui.engine.parser.utils.asArrayOrSingle
import com.example.drivenui.engine.parser.utils.asObjectOrNull
import com.example.drivenui.engine.parser.utils.int
import com.example.drivenui.engine.parser.utils.parseJsonObject
import com.example.drivenui.engine.parser.utils.string
import com.google.gson.JsonObject

/**
 * Парсер стилей из canvas JSON.
 */
class StyleParser {

    /**
     * Парсит все стили из JSON строки с корнем allStyles.
     * Списки стилей ожидаются как JSON arrays.
     * 
     * @return [AllStyles] контейнер со всеми типами стилей
     */
    fun parseStyles(jsonContent: String): AllStyles {
        val root = parseJsonObject(jsonContent)
            ?.get("allStyles")
            ?.asObjectOrNull()
            ?: JsonObject()

        return AllStyles(
            textStyles = root.objects("textStyles").map(::parseTextStyle),
            colorStyles = root.objects("colorStyles").map(::parseColorStyle),
            alignmentStyles = root.objects("alignmentStyles").map(::parseAlignmentStyle),
            paddingStyles = root.objects("paddingStyles").map(::parsePaddingStyle),
            roundStyles = root.objects("roundStyles").map(::parseRoundStyle),
        )
    }

    private fun parseTextStyle(json: JsonObject): TextStyle =
        TextStyle(
            code = json.string("code"),
            fontFamily = json.string("fontFamily", "fontName"),
            fontSize = json.int("fontSize"),
            fontWeight = json.int("fontWeight"),
        )

    private fun parseColorStyle(json: JsonObject): ColorStyle =
        ColorStyle(
            code = json.string("code"),
            lightTheme = parseColorTheme(json.get("lightTheme").asObjectOrNull()),
            darkTheme = parseColorTheme(json.get("darkTheme").asObjectOrNull()),
        )

    private fun parseColorTheme(json: JsonObject?): ColorTheme =
        ColorTheme(
            color = json?.string("color", default = "#000000") ?: "#000000",
            opacity = json?.int("opacity", default = 100) ?: 100,
        )

    private fun parseAlignmentStyle(json: JsonObject): AlignmentStyle =
        AlignmentStyle(code = json.string("code"))

    private fun parsePaddingStyle(json: JsonObject): PaddingStyle =
        PaddingStyle(
            code = json.string("code"),
            paddingLeft = json.int("paddingLeft"),
            paddingTop = json.int("paddingTop"),
            paddingRight = json.int("paddingRight"),
            paddingBottom = json.int("paddingBottom"),
        )

    private fun parseRoundStyle(json: JsonObject): RoundStyle =
        RoundStyle(
            code = json.string("code"),
            radiusValue = json.int("radiusValue"),
        )

    private fun JsonObject.objects(name: String): List<JsonObject> =
        get(name).asArrayOrSingle().mapNotNull { it.asObjectOrNull() }
}
