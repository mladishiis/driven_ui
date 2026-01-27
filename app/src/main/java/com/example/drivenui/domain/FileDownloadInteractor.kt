package com.example.drivenui.domain

import java.io.File

interface FileDownloadInteractor {
    suspend fun downloadAndExtractZip(url: String): Boolean
    suspend fun clearAssetsFolder(): Boolean
    fun getAssetsFileList(): List<String>
    suspend fun copyAssetFileToInternalStorage(filename: String): File
}