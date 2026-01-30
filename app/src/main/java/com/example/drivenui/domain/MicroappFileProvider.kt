package com.example.drivenui.domain

/**
 * Источник файлов микроаппа.
 * Domain знает ЧТО нужно, но не знает КАК читается.
 */
interface MicroappFileProvider {

    fun readMicroapp(): String
    fun readStyles(): String
    fun readQueries(): String

    /**
     * @return список экранов: fileName → xml
     */
    fun readScreens(): List<Pair<String, String>>
}