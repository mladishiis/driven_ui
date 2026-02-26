package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.cache.CachedComponentModel
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.serialization.CachedComponentTypeAdapter
import com.example.drivenui.engine.cache.serialization.UiActionTypeAdapter
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val TAG = "MicroappStorage"
private const val STORAGE_DIR = "parsed_microapps"
private const val FALLBACK_CODE = "template"

/**
 * Реализация хранения замапленных микроаппов в JSON-файлах.
 * Файлы хранятся в context.filesDir/parsed_microapps/{microappCode}.json
 */
internal class MicroappStorageImpl @Inject constructor(
    private val context: Context
) : MicroappStorage {

    private val storageDir: File
        get() = File(context.filesDir, STORAGE_DIR)

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(CachedComponentModel::class.java, CachedComponentTypeAdapter())
            .registerTypeAdapter(UiAction::class.java, UiActionTypeAdapter())
            .create()
    }

    override suspend fun saveMapped(data: CachedMicroappData): String? =
        withContext(Dispatchers.IO) {
            if (!data.hasData()) {
                Log.w(TAG, "Skip save: data has no content")
                return@withContext null
            }

            val code = data.microappCode.takeIf { it.isNotBlank() } ?: FALLBACK_CODE
            val safeFileName = sanitizeFileName(code) + ".json"

            try {
                storageDir.mkdirs()
                val json = gson.toJson(data)
                File(storageDir, safeFileName).writeText(json)
                Log.d(TAG, "Saved microapp: $code")
                code
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save microapp", e)
                null
            }
        }

    override suspend fun loadMapped(microappCode: String): CachedMicroappData? =
        withContext(Dispatchers.IO) {
            val safeFileName = sanitizeFileName(microappCode) + ".json"
            val file = File(storageDir, safeFileName)
            if (!file.exists()) {
                Log.d(TAG, "Microapp not found: $microappCode")
                return@withContext null
            }

            try {
                val json = file.readText()
                gson.fromJson(json, CachedMicroappData::class.java)
                    ?.takeIf { it.hasData() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load microapp: $microappCode", e)
                null
            }
        }

    override suspend fun getAllCodes(): List<String> =
        withContext(Dispatchers.IO) {
            if (!storageDir.exists()) return@withContext emptyList()
            storageDir.listFiles()
                ?.filter { it.isFile && it.extension == "json" }
                ?.map { it.nameWithoutExtension }
                ?: emptyList()
        }

    override suspend fun delete(microappCode: String) {
        withContext(Dispatchers.IO) {
            val safeFileName = sanitizeFileName(microappCode) + ".json"
            File(storageDir, safeFileName).delete()
        }
    }

    override suspend fun contains(microappCode: String): Boolean =
        withContext(Dispatchers.IO) {
            val safeFileName = sanitizeFileName(microappCode) + ".json"
            File(storageDir, safeFileName).exists()
        }

    private fun sanitizeFileName(code: String): String =
        code.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
}
