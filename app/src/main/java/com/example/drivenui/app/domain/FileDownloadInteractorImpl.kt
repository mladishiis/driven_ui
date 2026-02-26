package com.example.drivenui.app.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.app.data.MicroappRootFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class FileDownloadInteractorImpl @Inject constructor(
    private val context: Context,
    private val client: OkHttpClient
) : FileDownloadInteractor {

    override suspend fun downloadAndExtractZip(url: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Downloading zip from: $url")

                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        error("HTTP ${response.code}")
                    }

                    val body = response.body ?: error("Empty response body")

                    clearAssetsFolder()

                    ZipInputStream(body.byteStream()).use { zip ->
                        unzipSafely(zip)
                    }

                    // После успешной распаковки сохраняем имя первой подпапки как корень микроаппа
                    val microappsDir = getMicroappsDir()
                    val firstDir = microappsDir.listFiles()?.firstOrNull { it.isDirectory }
                    if (firstDir != null) {
                        MicroappRootFinder.saveMicroappRootName(context, firstDir.name)
                    } else {
                        Log.w(TAG, "No subdirectories found in microapps after unzip")
                    }

                    Log.d(TAG, "Zip extracted successfully")
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download/extract failed", e)
                false
            }
        }

    // ---------- unzip ----------

    /**
     * Безопасно распаковывает ZIP архив.
     * Продолжает работу даже если отдельные файлы не удалось распаковать.
     */
    private fun unzipSafely(zip: ZipInputStream) {
        val targetDir = getMicroappsDir().canonicalFile
        var entry = zip.nextEntry
        var extractedCount = 0
        var errorCount = 0

        while (entry != null) {
            try {
                val file = File(targetDir, entry.name)

                // 🔒 защита от Zip Slip
                val canonicalFile = file.canonicalFile
                if (!canonicalFile.path.startsWith(targetDir.path)) {
                    throw SecurityException("Zip entry outside target dir: ${entry.name}")
                }

                if (entry.isDirectory) {
                    canonicalFile.mkdirs()
                } else {
                    canonicalFile.parentFile?.mkdirs()
                    FileOutputStream(canonicalFile).use { output ->
                        zip.copyTo(output)
                    }
                    extractedCount++
                    if (extractedCount % 10 == 0) {
                        Log.d(TAG, "Extracted $extractedCount files...")
                    }
                }
            } catch (e: Exception) {
                errorCount++
                Log.e(TAG, "Failed to extract entry: ${entry.name}", e)
                // Продолжаем распаковку остальных файлов
            }

            zip.closeEntry()
            entry = zip.nextEntry
        }

        Log.d(TAG, "Extraction completed: $extractedCount files extracted, $errorCount errors")
    }

    // ---------- microapps directory ----------

    override suspend fun clearAssetsFolder(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val dir = getMicroappsDir()
                if (dir.exists()) {
                    dir.deleteRecursively()
                }
                dir.mkdirs()

                // При очистке также забываем старый корень микроаппа
                MicroappRootFinder.clearSavedMicroappRoot(context)

                Log.d(TAG, "Microapps directory cleared")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear microapps directory", e)
                false
            }
        }

    override fun getAssetsFileList(): List<String> =
        try {
            val realAssets = context.assets.list("")?.toList() ?: emptyList()
            val microapps = getMicroappsDir()
                .listFiles()
                ?.map { it.name }
                ?: emptyList()

            (realAssets + microapps).distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Error listing assets", e)
            emptyList()
        }

    override suspend fun copyAssetFileToInternalStorage(filename: String): File =
        withContext(Dispatchers.IO) {
            val outputFile = File(context.filesDir, "extracted_assets/$filename")
            outputFile.parentFile?.mkdirs()

            val microappFile = File(getMicroappsDir(), filename)
            if (microappFile.exists()) {
                microappFile.copyTo(outputFile, overwrite = true)
                return@withContext outputFile
            }

            context.assets.open(filename).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            outputFile
        }

    // ---------- helpers ----------

    private fun getMicroappsDir(): File =
        File(context.filesDir, MICROAPPS_DIR)

    companion object {
        private const val TAG = "FileDownloadInteractor"
        private const val MICROAPPS_DIR = "microapps"
    }
}