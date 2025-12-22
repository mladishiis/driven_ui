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
            null
        }
    }

    /**
     * Парсит элемент microapp из XML
     *
     * @param parser XmlPullParser позиционированный на теге <microapp>
     * @return [Microapp] объект
     */
    private fun parseMicroappElement(parser: XmlPullParser): Microapp {
        val title = parser.getAttributeValue(null, "title") ?: ""
        var code = ""
        var shortCode = ""
        var deeplink = ""
        val persistents = mutableListOf<String>()

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "microapp")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "code" -> code = parser.nextText()
                        "shortСode" -> shortCode = parser.nextText()
                        "deeplink" -> deeplink = parser.nextText()
                        "persistent" -> persistents.add(parser.nextText())
                    }
                }
            }
            eventType = parser.next()
        }

        return Microapp(title, code, shortCode, deeplink, persistents)
    }
}