package com.example.drivenui.app.domain

import java.io.File

/**
 * Интерактор загрузки файлов микроаппов.
 * Скачивает архивы, распаковывает, управляет папкой assets.
 */
interface FileDownloadInteractor {

    /**
     * Скачивает ZIP по URL и распаковывает в папку микроаппов.
     *
     * @param url URL архива
     * @param format Формат загрузки (по умолчанию OCTET_STREAM)
     * 
     * @return true при успехе
     */
    suspend fun downloadAndExtractZip(url: String, format: ArchiveDownloadFormat = ArchiveDownloadFormat.OCTET_STREAM): Boolean

    /**
     * Очищает папку с ассетами микроаппов.
     *
     * @return true при успехе
     */
    suspend fun clearAssetsFolder(): Boolean

    /**
     * Возвращает список файлов в папке assets микроаппов.
     *
     * @return Список имён файлов
     */
    fun getAssetsFileList(): List<String>

    /**
     * Копирует файл из assets в внутреннее хранилище.
     *
     * @param filename Имя файла в assets
     * 
     * @return Файл во внутреннем хранилище
     */
    suspend fun copyAssetFileToInternalStorage(filename: String): File
}