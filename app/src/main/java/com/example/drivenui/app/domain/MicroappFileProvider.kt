package com.example.drivenui.app.domain

/**
 * Источник файлов микроаппа.
 * Domain слой знает ЧТО нужно читать, но не КАК (реализация в data).
 */
interface MicroappFileProvider {

    /**
     * Читает microapp.xml.
     *
     * @return содержимое microapp.xml
     */
    fun readMicroapp(): String

    /**
     * Читает allStyles.xml.
     *
     * @return содержимое allStyles.xml
     */
    fun readStyles(): String

    /**
     * Читает XML экранов.
     *
     * @return список пар (имя файла, xml-содержимое)
     */
    fun readScreens(): List<Pair<String, String>>

    /**
     * Читает microapp.xml. При отсутствии файла возвращает пустую строку (для шаблона).
     *
     * @return содержимое microapp.xml или пустая строка
     */
    fun readMicroappOrEmpty(): String
}