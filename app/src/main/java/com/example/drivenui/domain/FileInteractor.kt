package com.example.drivenui.domain

import com.example.drivenui.parser.SDUIParser
import org.json.JSONArray

/**
 * Интерфейс для работы с файлами и парсингом SDUI
 */
interface FileInteractor {

    /**
     * Парсит файл из assets с новой структурой компонентов
     * @param fileName Имя файла в папке assets
     * @return Результат парсинга с новой структурой
     */
    suspend fun parseMicroappFromAssetsRoot(): SDUIParser.ParsedMicroappResult

    /**
     * Получает список доступных файлов в assets
     * @return Список имен файлов
     */
    fun getAvailableFiles(): List<String>

    /**
     * Получает список JSON файлов в assets
     * @return Список имен JSON файлов
     */
    fun getAvailableJsonFiles(): List<String>

    /**
     * Загружает XML файл как строку
     * @param fileName Имя файла
     * @return Содержимое файла как строка
     */
    suspend fun loadXmlFile(fileName: String): String

    /**
     * Загружает JSON файл как строку
     * @param fileName Имя JSON файла
     * @return Содержимое файла как строка
     */
    suspend fun loadJsonFile(fileName: String): String

    /**
     * Сохраняет результат парсинга
     * @param parsedMicroapp Результат парсинга
     */
    fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult)

    /**
     * Получает последний результат парсинга
     * @return Последний результат или null
     */
    fun getLastParsedResult(): SDUIParser.ParsedMicroappResult?

    /**
     * Получает разрешенные значения биндингов
     * @return Мапа разрешенных биндингов
     */
    fun getResolvedValues(): Map<String, String>

    /**
     * Валидирует результат парсинга
     * @param result Результат для валидации
     * @return true если результат валиден
     */
    suspend fun validateParsingResult(result: SDUIParser.ParsedMicroappResult): Boolean

    /**
     * Получает статистику парсинга
     * @return Статистика или null если нет данных
     */
    fun getParsingStats(): Map<String, Any>?

    /**
     * Получает детальную статистику по биндингам
     * @return Статистика биндингов или null
     */
    fun getBindingStats(): Map<String, Any>?

    /**
     * Очищает сохраненные данные парсинга
     */
    fun clearParsedData()

    suspend fun loadJsonFileAsArray(fileName: String): JSONArray?
}