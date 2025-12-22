package com.example.drivenui.domain

import com.example.drivenui.parser.SDUIParser

/**
 * Интерфейс для работы с файлами и парсингом SDUI
 */
interface FileInteractor {

    /**
     * Загружает и парсит файл из assets
     * @param fileName Имя файла в папке assets
     * @return Результат парсинга
     */
    suspend fun parseFileFromAssets(fileName: String): SDUIParser.ParsedMicroapp

    /**
     * Загружает и парсит файл из ZIP архива
     * @param filePath Путь к ZIP файлу
     * @return Результат парсинга
     */
    suspend fun parseFileFromZip(filePath: String): SDUIParser.ParsedMicroapp

    /**
     * Получает список доступных файлов в assets
     * @return Список имен файлов
     */
    fun getAvailableFiles(): List<String>

    /**
     * Загружает XML файл из assets
     * @param fileName Имя файла
     * @return Содержимое файла в виде строки
     */
    suspend fun loadXmlFile(fileName: String): String

    /**
     * Сохраняет результат парсинга
     * @param parsedMicroapp Результат парсинга
     */
    fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroapp)

    /**
     * Получает последний результат парсинга
     * @return Результат парсинга или null
     */
    fun getLastParsedResult(): SDUIParser.ParsedMicroapp?

    /**
     * Валидирует результат парсинга микроаппа.
     *
     * @param result Результат парсинга для проверки
     * @return true если данные валидны и могут быть использованы
     */
    suspend fun validateParsingResult(result: SDUIParser.ParsedMicroapp): Boolean

    /**
     * Возвращает статистику последнего парсинга.
     *
     * @return Карта со статистикой или null если парсинг не выполнялся
     */
    fun getParsingStats(): Map<String, Any>?

    /**
     * Очищает кэшированные данные парсинга.
     */
    fun clearParsedData()
}