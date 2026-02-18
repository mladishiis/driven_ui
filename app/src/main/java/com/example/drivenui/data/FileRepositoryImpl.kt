package com.example.drivenui.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Реализация FileRepository чтение/запись файлов.
 */
internal class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    /**
     * Динамически определяет корневую папку микроаппа при каждом обращении.
     * Если микроапп не найден, выбрасывает исключение —
     * это означает, что архив ещё не был загружен/распакован.
     */
    private fun getRootDir(): File {
        val foundRoot = MicroappRootFinder.findMicroappRoot(context)
        return foundRoot
            ?: error("Microapp root directory not found in 'microapps'. Please download and extract microapp archive.")
    }

    override fun getAvailableFiles(): List<String> =
        try {
            getRootDir().list()?.toList().orEmpty()
        } catch (e: Exception) {
            Log.e("FileRepository", "Error listing files", e)
            emptyList()
        }

    override fun getAvailableJsonFiles(): List<String> =
        try {
            getRootDir().list()?.filter { it.endsWith(".json") }.orEmpty()
        } catch (e: Exception) {
            Log.e("FileRepository", "Error listing JSON files", e)
            emptyList()
        }

    override suspend fun loadXmlFile(fileName: String): String =
        withContext(Dispatchers.IO) {
            val file = File(getRootDir(), fileName)
            if (!file.exists()) {
                throw FileNotFoundException("XML file not found: $fileName")
            }
            file.readText()
        }

    override suspend fun loadJsonFile(fileName: String): String =
        withContext(Dispatchers.IO) {
            val file = File(getRootDir(), fileName)
            if (!file.exists()) {
                throw FileNotFoundException("JSON file not found: $fileName")
            }
            file.readText()
        }

    override suspend fun saveJsonResult(jsonResult: String, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val rootDir = getRootDir()
                if (!rootDir.exists()) {
                    rootDir.mkdirs()
                }

                val file = File(rootDir, fileName)
                file.writeText(jsonResult)

                Log.d(
                    "FileRepository",
                    "Saved file: ${file.absolutePath}"
                )
            } catch (e: Exception) {
                Log.e(
                    "FileRepository",
                    "Error saving file $fileName",
                    e
                )
                throw e
            }
        }
    }
}