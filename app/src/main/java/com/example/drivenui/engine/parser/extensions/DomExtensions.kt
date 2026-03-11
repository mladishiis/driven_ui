package com.example.drivenui.engine.parser.extensions

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

/**
 * Расширения для удобной работы с DOM-деревом.
 */

/**
 * Преобразует NodeList в список [Node].
 *
 * @return Список узлов
 */
fun NodeList.toList(): List<Node> {
    val list = mutableListOf<Node>()
    for (i in 0 until length) {
        item(i)?.let { list.add(it) }
    }
    return list
}

/**
 * Преобразует NodeList в список [Element].
 *
 * @return Список элементов
 */
fun NodeList.toElementList(): List<Element> {
    val list = mutableListOf<Element>()
    for (i in 0 until length) {
        val item = item(i)
        if (item is Element) {
            list.add(item)
        }
    }
    return list
}

/**
 * Возвращает дочерние элементы с указанным именем тега в виде списка.
 *
 * @param tagName Имя тега для поиска
 * @return Список элементов с указанным именем
 */
fun Element.getElementsByTagNameAsList(tagName: String): List<Element> {
    return getElementsByTagName(tagName).toElementList()
}