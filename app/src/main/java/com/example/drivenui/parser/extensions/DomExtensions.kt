package com.example.drivenui.parser.extensions

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

/**
 * Расширения для удобной работы с DOM
 */

// Расширение для NodeList
fun NodeList.toList(): List<Node> {
    val list = mutableListOf<Node>()
    for (i in 0 until length) {
        item(i)?.let { list.add(it) }
    }
    return list
}

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

// Расширение для Element
fun Element.getElementsByTagNameAsList(tagName: String): List<Element> {
    return getElementsByTagName(tagName).toElementList()
}