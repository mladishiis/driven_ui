package com.example.drivenui.data

/**
 * Репозиторий для низкоуровневого доступа к файловой системе и assets.
 *
 * Предоставляет методы чтения и записи файлов, включая XML и JSON.
 * Этот интерфейс не содержит бизнес-логики и служит исключительно
 * для работы с файловыми ресурсами.
 */
interface FileRepository {

    /**
     * Возвращает список всех доступных файлов.
     *
     * @return список имён файлов (без путей)
     */
    fun getAvailableFiles(): List<String>

    /**
     * Возвращает список всех доступных JSON-файлов.
     *
     * Используется для загрузки mock-данных или конфигураций.
     *
     * @return список имён JSON-файлов
     */
    fun getAvailableJsonFiles(): List<String>

    /**
     * Загружает содержимое XML-файла по имени.
     *
     * @param fileName имя XML-файла
     * @return содержимое файла в виде строки
     * @throws Exception если файл не найден или произошла ошибка чтения
     */
    suspend fun loadXmlFile(fileName: String): String

    /**
     * Загружает содержимое JSON-файла по имени.
     *
     * @param fileName имя JSON-файла
     * @return содержимое файла в виде строки
     * @throws Exception если файл не найден или произошла ошибка чтения
     */
    suspend fun loadJsonFile(fileName: String): String

    /**
     * Сохраняет результат в JSON-файл.
     *
     * @param jsonResult содержимое JSON, которое нужно сохранить
     * @param fileName имя файла для сохранения
     * @throws Exception если не удалось записать файл
     */
    suspend fun saveJsonResult(jsonResult: String, fileName: String)

    /**
     * Метаданные файла.
     *
     * Используется для получения информации о доступных файлах:
     * имя, размер, время последнего изменения и тип.
     *
     * @property name имя файла
     * @property size размер файла в байтах
     * @property lastModified время последнего изменения в миллисекундах с эпохи
     * @property type тип файла (например, "xml", "json", "txt")
     */
    data class FileInfo(
        val name: String,
        val size: Long,
        val lastModified: Long,
        val type: String
    )
}