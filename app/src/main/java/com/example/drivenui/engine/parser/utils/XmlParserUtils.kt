package com.example.drivenui.engine.parser.utils

import org.xmlpull.v1.XmlPullParser

/**
 * Утилиты для работы с XML парсером
 */
object XmlParserUtils {

    /**
     * Читает текст текущего элемента XML, пропуская вложенные теги
     *
     * @param parser XmlPullParser
     * @return Текст элемента или пустая строка
     */
    fun readTextSafely(parser: XmlPullParser): String {
        return try {
            parser.nextText()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Переходит к следующему тегу указанного типа
     *
     * @param parser XmlPullParser
     * @param tagName Имя тега для поиска
     * @return true если тег найден, false если достигнут конец документа
     */
    fun skipToTag(parser: XmlPullParser, tagName: String): Boolean {
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == tagName) {
                return true
            }
            eventType = parser.next()
        }

        return false
    }

    /**
     * Извлекает значение атрибута или возвращает значение по умолчанию
     *
     * @param parser XmlPullParser
     * @param attributeName Имя атрибута
     * @param defaultValue Значение по умолчанию
     * @return Значение атрибута или значение по умолчанию
     */
    fun getAttributeValue(
        parser: XmlPullParser,
        attributeName: String,
        defaultValue: String = ""
    ): String {
        return parser.getAttributeValue(null, attributeName) ?: defaultValue
    }

    /**
     * Проверяет, находится ли парсер на указанном теге
     *
     * @param parser XmlPullParser
     * @param tagName Имя тега для проверки
     * @return true если текущий тег соответствует указанному имени
     */
    fun isOnTag(parser: XmlPullParser, tagName: String): Boolean {
        return parser.eventType == XmlPullParser.START_TAG && parser.name == tagName
    }

    /**
     * Пропускает текущий элемент и все его дочерние элементы
     *
     * @param parser XmlPullParser
     */
    fun skipCurrentElement(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException("Parser is not on START_TAG")
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    /**
     * Читает весь текст внутри элемента, включая вложенные CDATA
     *
     * @param parser XmlPullParser позиционированный на START_TAG
     * @return Весь текст элемента
     */
    fun readFullText(parser: XmlPullParser): String {
        val text = StringBuilder()
        var eventType = parser.next()

        while (!(eventType == XmlPullParser.END_TAG && parser.depth == parser.depth - 1)) {
            when (eventType) {
                XmlPullParser.TEXT -> text.append(parser.text)
                XmlPullParser.CDSECT -> text.append(parser.text)
            }
            eventType = parser.next()
        }

        return text.toString()
    }
}