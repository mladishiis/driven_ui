package com.example.drivenui.parser.parsers

import android.util.Log
import com.example.drivenui.parser.extensions.getElementsByTagNameAsList
import com.example.drivenui.parser.extensions.toElementList
import com.example.drivenui.parser.models.QueryProperty
import com.example.drivenui.parser.models.ScreenQuery
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Парсер screen queries
 */
class ScreenQueryParser {

    fun parseScreenQueries(xmlContent: String): List<ScreenQuery> {
        return try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(InputSource(StringReader(xmlContent)))

            document.getElementsByTagName("screenQuery").toElementList().map { element ->
                ScreenQuery(
                    code = element.getElementsByTagNameAsList("code").firstOrNull()?.textContent ?: "",
                    screenCode = element.getElementsByTagNameAsList("screenCode").firstOrNull()?.textContent ?: "",
                    queryCode = element.getElementsByTagNameAsList("queryCode").firstOrNull()?.textContent ?: "",
                    order = element.getElementsByTagNameAsList("order").firstOrNull()?.textContent?.toIntOrNull() ?: 0,
                    properties = parseQueryProperties(element),
                    conditions = emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("ScreenQueryParser", "Ошибка парсинга screen queries", e)
            emptyList()
        }
    }

    private fun parseQueryProperties(element: Element): List<QueryProperty> {
        return element.getElementsByTagNameAsList("property").mapNotNull { propertyElement ->
            val code = propertyElement.getElementsByTagNameAsList("code").firstOrNull()?.textContent ?: return@mapNotNull null
            val variableName = propertyElement.getElementsByTagNameAsList("variableName").firstOrNull()?.textContent ?: return@mapNotNull null
            val variableValue = propertyElement.getElementsByTagNameAsList("variableValue").firstOrNull()?.textContent ?: ""

            QueryProperty(code, variableName, variableValue)
        }
    }
}