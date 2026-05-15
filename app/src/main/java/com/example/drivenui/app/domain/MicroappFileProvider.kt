package com.example.drivenui.app.domain

/**
 * Источник файлов микроаппа.
 */
interface MicroappFileProvider {

    /**
     * Читает microapp.json.
     *
     * @return содержимое microapp.json
     */
    fun readMicroapp(): String

    /**
     * Читает allStyles.json.
     *
     * @return содержимое allStyles.json
     */
    fun readStyles(): String

    /**
     * Читает JSON экранов.
     *
     * @return список пар (имя файла, json-содержимое)
     */
    fun readScreens(): List<Pair<String, String>>

    /**
     * Читает microapp.json. При отсутствии файла возвращает пустую строку (для шаблона).
     *
     * @return содержимое microapp.json или пустая строка
     */
    fun readMicroappOrEmpty(): String
}