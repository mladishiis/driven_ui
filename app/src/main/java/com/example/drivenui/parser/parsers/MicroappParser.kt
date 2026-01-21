package com.example.drivenui.parser.parsers

import com.example.drivenui.parser.models.Microapp
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Парсер модели микроаппа из XML формата Driven UI
 */
class MicroappParser {

    /**
     * Парсит модель микроаппа из XML строки
     *
     * @param xmlContent XML строка содержащая блок <microapp>
     * @return [Microapp] объект или null если парсинг не удался
     */
    fun parseMicroapp(xmlContent: String): Microapp? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(xmlContent.reader())

            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "microapp") {
                    return parseMicroappElement(parser)
                }
                eventType = parser.next()
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseMicroappElement(parser: XmlPullParser): Microapp {

        val title = parser.getAttributeValue(null, "title") ?: ""

        var code = ""
        var shortCode = ""
        var deeplink = ""
        val persistents = mutableListOf<String>()

        var eventType = parser.next()

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "microapp")) {

            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {

                    "code" -> {
                        code = parser.nextText()
                    }

                    "shortCode" -> {   // ✅ исправлено
                        shortCode = parser.nextText()
                    }

                    "deeplink" -> {
                        deeplink = parser.nextText()
                    }

                    "persistent" -> {
                        val value = parser.nextText().trim()
                        if (value.isNotEmpty()) {
                            persistents.add(value)
                        }
                    }

                    // screens мы сознательно ПРОПУСКАЕМ
                    "screens" -> {
                        skipSubTree(parser)
                    }
                }
            }

            eventType = parser.next()
        }

        return Microapp(
            title = title,
            code = code,
            shortCode = shortCode,
            deeplink = deeplink,
            persistents = persistents
        )
    }

    /**
     * Безопасно пропускает вложенное дерево XML
     * (например <screens> ... </screens>)
     */
    private fun skipSubTree(parser: XmlPullParser) {
        var depth = 1

        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }
}
