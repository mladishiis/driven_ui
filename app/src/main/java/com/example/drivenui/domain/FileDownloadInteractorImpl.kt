package com.example.drivenui.domain

import android.content.Context
import android.util.Log
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

                    Log.d(TAG, "Zip extracted successfully")
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download/extract failed", e)
                false
            }
        }

    // ---------- unzip ----------

    private fun unzipSafely(zip: ZipInputStream) {
        val targetDir = getAssetsSimulationDir().canonicalFile
        var entry = zip.nextEntry

        while (entry != null) {
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
                Log.d(TAG, "Extracted: ${entry.name}")
            }

            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    // ---------- assets_simulation ----------

    override suspend fun clearAssetsFolder(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val dir = getAssetsSimulationDir()
                if (dir.exists()) {
                    dir.deleteRecursively()
                }
                dir.mkdirs()
                Log.d(TAG, "assets_simulation cleared")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear assets_simulation", e)
                false
            }
        }

    override fun getAssetsFileList(): List<String> =
        try {
            val realAssets = context.assets.list("")?.toList() ?: emptyList()
            val simulated = getAssetsSimulationDir()
                .listFiles()
                ?.map { it.name }
                ?: emptyList()

            (realAssets + simulated).distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Error listing assets", e)
            emptyList()
        }

    override suspend fun copyAssetFileToInternalStorage(filename: String): File =
        withContext(Dispatchers.IO) {
            val outputFile = File(context.filesDir, "extracted_assets/$filename")
            outputFile.parentFile?.mkdirs()

            val simulated = File(getAssetsSimulationDir(), filename)
            if (simulated.exists()) {
                simulated.copyTo(outputFile, overwrite = true)
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

    private fun getAssetsSimulationDir(): File =
        File(context.filesDir, ASSETS_SIMULATION_DIR)

    companion object {
        private const val TAG = "FileDownloadInteractor"
        private const val ASSETS_SIMULATION_DIR = "assets_simulation"
    }
}