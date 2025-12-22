package com.example.drivenui.data

/**
 * Репозиторий для работы с файлами
 */
interface FileRepository {

    /**
     * Загружает XML контент из файла
     * @param fileName Имя файла в assets
     * @return XML контент в виде строки
     */
    suspend fun loadXmlContent(fileName: String): String

    /**
     * Сохраняет JSON результат парсинга
     * @param jsonResult Результат в формате JSON
     * @param fileName Имя файла для сохранения
     */
    suspend fun saveJsonResult(jsonResult: String, fileName: String)

    /**
     * Получает список XML файлов в assets
     * @return Список имен файлов
     */
    fun getXmlFiles(): List<String>

    /**
     * Проверяет существование файла
     * @param fileName Имя файла
     * @return true если файл существует
     */
    fun fileExists(fileName: String): Boolean

    /**
     * Получает информацию о файле
     * @param fileName Имя файла
     * @return Информация о файле или null
     */
    suspend fun getFileInfo(fileName: String): FileInfo?

    /**
     * Модель информации о файле
     */
    data class FileInfo(
        val name: String,
        val size: Long,
        val lastModified: Long,
        val type: String
    )
}