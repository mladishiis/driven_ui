package com.example.drivenui.domain

import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.parser.models.ParsedScreen
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
    suspend fun parseFileFromAssets(fileName: String): SDUIParser.ParsedMicroappResult

    /**
     * Парсит файл с поддержкой JSON данных для биндингов
     * @param fileName Имя файла в папке assets
     * @param jsonFileNames Список имен JSON файлов для биндингов
     * @return Результат парсинга с разрешенными биндингами
     */
    suspend fun parseFileFromAssets(
        fileName: String,
        jsonFileNames: List<String>
    ): SDUIParser.ParsedMicroappResult

    /**
     * Парсит специфичный экран с данными (например, carriers)
     * @return Экран carriers с данными или null
     */
    suspend fun parseCarriersScreenWithData(): ParsedScreen?

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

    suspend fun parseWithCustomData(
        fileName: String,
        jsonData: Map<String, String>,
        queryResults: Map<String, Any>,
        screenQueryResults: Map<String, Any>
    ): SDUIParser.ParsedMicroappResult

    suspend fun loadJsonFileAsArray(fileName: String): JSONArray?
}