package com.example.drivenui.app.domain

import java.io.File

interface FileDownloadInteractor {
    suspend fun downloadAndExtractZip(url: String, format: ArchiveDownloadFormat = ArchiveDownloadFormat.OCTET_STREAM): Boolean
    suspend fun clearAssetsFolder(): Boolean
    fun getAssetsFileList(): List<String>
    suspend fun copyAssetFileToInternalStorage(filename: String): File
}