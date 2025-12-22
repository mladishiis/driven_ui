package com.example.drivenui.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Реализация FileRepository
 */
internal class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    override suspend fun loadXmlContent(fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e("FileRepository", "Ошибка при загрузке файла $fileName", e)
                throw e
            }
        }
    }

    override suspend fun saveJsonResult(jsonResult: String, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                file.writeText(jsonResult)
                Log.d("FileRepository", "Файл сохранен: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("FileRepository", "Ошибка при сохранении файла $fileName", e)
                throw e
            }
        }
    }

    override fun getXmlFiles(): List<String> {
        return try {
            context.assets.list("")?.filter {
                it.endsWith(".xml") || it.endsWith(".zip")
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e("FileRepository", "Ошибка при получении списка файлов", e)
            emptyList()
        }
    }

    override fun fileExists(fileName: String): Boolean {
        return try {
            context.assets.open(fileName).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getFileInfo(fileName: String): FileRepository.FileInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(fileName)
                val size = inputStream.available().toLong()
                inputStream.close()

                FileRepository.FileInfo(
                    name = fileName,
                    size = size,
                    lastModified = System.currentTimeMillis(),
                    type = when {
                        fileName.endsWith(".xml") -> "XML"
                        fileName.endsWith(".zip") -> "ZIP"
                        else -> "UNKNOWN"
                    }
                )
            } catch (e: Exception) {
                Log.e("FileRepository", "Ошибка при получении информации о файле $fileName", e)
                null
            }
        }
    }
}