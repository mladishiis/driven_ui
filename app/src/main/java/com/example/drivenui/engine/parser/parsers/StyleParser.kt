package com.example.drivenui.engine.parser.parsers

import com.example.drivenui.engine.parser.models.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер стилей из XML формата Driven UI
 *
 * Поддерживает парсинг всех типов стилей: textStyles, colorStyles,
 * alignmentStyles, paddingStyles, roundStyles
 */
class StyleParser {

    /**
     * Парсит все стили из XML строки
     *
     * @param xmlContent XML строка содержащая блок <allStyles>
     * @return [AllStyles] контейнер со всеми типами стилей
     */
    fun parseStyles(xmlContent: String): AllStyles {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlContent.reader())

        val textStyles = mutableListOf<TextStyle>()
        val colorStyles = mutableListOf<ColorStyle>()
        val alignmentStyles = mutableListOf<AlignmentStyle>()
        val paddingStyles = mutableListOf<PaddingStyle>()
        val roundStyles = mutableListOf<RoundStyle>()

        var eventType = parser.eventType
        var currentElement = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentElement = parser.name
                    when (parser.name) {
                        "textStyle" -> {
                            textStyles.add(parseTextStyle(parser))
                        }
                        "colorStyle" -> {
                            colorStyles.add(parseColorStyle(parser))
                        }
                        "alignStyle" -> {
                            alignmentStyles.add(parseAlignmentStyle(parser))
                        }
                        "paddingStyle" -> {
                            paddingStyles.add(parsePaddingStyle(parser))
                        }
                        "roundStyle" -> {
                            roundStyles.add(parseRoundStyle(parser))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return AllStyles(
            textStyles = textStyles,
            colorStyles = colorStyles,
            alignmentStyles = alignmentStyles,
            paddingStyles = paddingStyles,
            roundStyles = roundStyles,
        )
    }

    /**
     * Парсит отдельный стиль текста из XML
     *
     * @param parser XmlPullParser позиционированный на теге <textStyle>
     * @return [TextStyle] объект стиля текста
     */
    private fun parseTextStyle(parser: XmlPullParser): TextStyle {
        var code = ""
        var fontFamily = ""
        var fontSize = 0
        var fontWeight = 0

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "textStyle")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "fontFamily" -> fontFamily = parser.nextText()
                        "fontSize" -> fontSize = parser.nextText().toIntOrNull() ?: 0
                        "fontWeight" -> fontWeight = parser.nextText().toIntOrNull() ?: 0
                    }
                }
            }
            eventType = parser.next()
        }

        return TextStyle(code, fontFamily, fontSize, fontWeight)
    }

    /**
     * Парсит отдельный стиль цвета из XML
     *
     * @param parser XmlPullParser позиционированный на теге <colorStyle>
     * @return [ColorStyle] объект стиля цвета
     */
    private fun parseColorStyle(parser: XmlPullParser): ColorStyle {
        var code = ""
        var lightTheme = ColorTheme("#000000")
        var darkTheme = ColorTheme("#000000")

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "colorStyle")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "lightTheme" -> lightTheme = parseColorTheme(parser)
                        "darkTheme" -> darkTheme = parseColorTheme(parser)
                    }
                }
            }
            eventType = parser.next()
        }

        return ColorStyle(code, lightTheme, darkTheme)
    }

    /**
     * Парсит цветовую тему (светлую или темную) из XML
     *
     * @param parser XmlPullParser позиционированный на теге <lightTheme> или <darkTheme>
     * @return [ColorTheme] объект цветовой темы
     */
    private fun parseColorTheme(parser: XmlPullParser): ColorTheme {
        var color = "#000000"
        var opacity = 100

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG &&
                    (parser.name == "lightTheme" || parser.name == "darkTheme"))) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "color" -> color = parser.nextText()
                        "opacity" -> opacity = parser.nextText().toIntOrNull() ?: 100
                    }
                }
            }
            eventType = parser.next()
        }

        return ColorTheme(color, opacity)
    }

    /**
     * Парсит отдельный стиль выравнивания из XML
     *
     * @param parser XmlPullParser позиционированный на теге <alignStyle>
     * @return [AlignmentStyle] объект стиля выравнивания
     */
    private fun parseAlignmentStyle(parser: XmlPullParser): AlignmentStyle {
        var code = ""

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "alignStyle")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                    }
                }
            }
            eventType = parser.next()
        }

        return AlignmentStyle(code)
    }

    /**
     * Парсит отдельный стиль отступов из XML
     *
     * @param parser XmlPullParser позиционированный на теге <paddingStyle>
     * @return [PaddingStyle] объект стиля отступов
     */
    private fun parsePaddingStyle(parser: XmlPullParser): PaddingStyle {
        var code = ""
        var paddingLeft = 0
        var paddingTop = 0
        var paddingRight = 0
        var paddingBottom = 0

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "paddingStyle")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "paddingLeft" -> paddingLeft = parser.nextText().toIntOrNull() ?: 0
                        "paddingTop" -> paddingTop = parser.nextText().toIntOrNull() ?: 0
                        "paddingRight" -> paddingRight = parser.nextText().toIntOrNull() ?: 0
                        "paddingBottom" -> paddingBottom = parser.nextText().toIntOrNull() ?: 0
                    }
                }
            }
            eventType = parser.next()
        }

        return PaddingStyle(code, paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    /**
     * Парсит отдельный стиль скругления из XML
     *
     * @param parser XmlPullParser позиционированный на теге <roundStyle>
     * @return [RoundStyle] объект стиля скругления
     */
    private fun parseRoundStyle(parser: XmlPullParser): RoundStyle {
        var code = ""
        var radiusValue = 0

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "roundStyle")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "radiusValue" -> radiusValue = parser.nextText().toIntOrNull() ?: 0
                    }
                }
            }
            eventType = parser.next()
        }

        return RoundStyle(code, radiusValue)
    }
}