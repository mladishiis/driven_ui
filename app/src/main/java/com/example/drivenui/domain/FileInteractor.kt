package com.example.drivenui.domain

import com.example.drivenui.parser.SDUIParserNew

/**
 * Интерфейс для работы с файлами и парсингом SDUI
 */
interface FileInteractor {

    /**
     * Парсит файл из assets с новой структурой компонентов
     * @param fileName Имя файла в папке assets
     * @return Результат парсинга с новой структурой
     */
    suspend fun parseFileFromAssets(fileName: String): SDUIParserNew.ParsedMicroappResult

    /**
     * Парсит ZIP файл с новой структурой компонентов
     * @param filePath Путь к ZIP файлу
     * @return Результат парсинга с новой структурой
     */
    suspend fun parseFileFromZip(filePath: String): SDUIParserNew.ParsedMicroappResult

    /**
     * Получает список доступных файлов в assets
     * @return Список имен файлов
     */
    fun getAvailableFiles(): List<String>

    /**
     * Загружает XML файл как строку
     * @param fileName Имя файла
     * @return Содержимое файла как строка
     */
    suspend fun loadXmlFile(fileName: String): String

    /**
     * Сохраняет результат парсинга
     * @param parsedMicroapp Результат парсинга
     */
    fun saveParsedResult(parsedMicroapp: SDUIParserNew.ParsedMicroappResult)

    /**
     * Получает последний результат парсинга
     * @return Последний результат или null
     */
    fun getLastParsedResult(): SDUIParserNew.ParsedMicroappResult?

    /**
     * Валидирует результат парсинга
     * @param result Результат для валидации
     * @return true если результат валиден
     */
    suspend fun validateParsingResult(result: SDUIParserNew.ParsedMicroappResult): Boolean

    /**
     * Получает статистику парсинга
     * @return Статистика или null если нет данных
     */
    fun getParsingStats(): Map<String, Any>?

    /**
     * Очищает сохраненные данные парсинга
     */
    fun clearParsedData()
}