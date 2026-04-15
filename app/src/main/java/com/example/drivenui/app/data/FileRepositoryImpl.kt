package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Реализация FileRepository для чтения/записи файлов микроаппа.
 *
 * @property context контекст приложения для доступа к filesDir
 */
internal class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    /** Корневая папка микроаппа в файловой системе. */
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
            } catch (e: Exception) {
                Log.e(
                    "FileRepository",
                    "Ошибка сохранения файла $fileName",
                    e
                )
                throw e
            }
        }
    }
}