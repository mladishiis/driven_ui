package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.engine.cache.CachedComponentModel
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.serialization.CachedComponentTypeAdapter
import com.example.drivenui.engine.cache.serialization.UiActionTypeAdapter
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

private const val TAG = "MicroappStorage"
private const val STORAGE_DIR = "parsed_microapps"
private const val META_DIR = "meta"
private const val FALLBACK_CODE = "template"
private const val FILE_COLLECTION_ID = "collection_id"
private const val FILE_COLLECTION_CODES = "collection_codes.json"
private const val FILE_SINGLE_LIST_CODES = "single_list_codes.json"

private const val LEGACY_PREFS = "collection_prefs"
private const val LEGACY_KEY_COLLECTION_ID = "collection_id"
private const val LEGACY_KEY_COLLECTION_CODES = "collection_codes"
private const val LEGACY_KEY_SINGLE_LIST_CODES = "single_list_codes"

/**
 * Реализация хранения замапленных микроаппов в JSON-файлах.
 * Файлы хранятся в context.filesDir/parsed_microapps/{microappCode}.json
 *
 * @property context контекст приложения для доступа к filesDir и SharedPreferences
 */
internal class MicroappStorageImpl @Inject constructor(
    private val context: Context
) : MicroappStorage {

    private val storageDir: File
        get() = File(context.filesDir, STORAGE_DIR)

    private val metaDir: File
        get() = File(storageDir, META_DIR)

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(CachedComponentModel::class.java, CachedComponentTypeAdapter())
            .registerTypeAdapter(UiAction::class.java, UiActionTypeAdapter())
            .create()
    }

    override suspend fun saveMapped(data: CachedMicroappData): String? =
        withContext(Dispatchers.IO) {
            if (!data.hasData()) {
                Log.w(TAG, "Пропуск сохранения: в данных нет содержимого")
                return@withContext null
            }

            val code = data.microappCode.takeIf { it.isNotBlank() } ?: FALLBACK_CODE
            val safeFileName = sanitizeFileName(code) + ".json"

            try {
                storageDir.mkdirs()
                val json = gson.toJson(data)
                File(storageDir, safeFileName).writeText(json)
                code
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось сохранить микроапп", e)
                null
            }
        }

    override suspend fun loadMapped(microappCode: String): CachedMicroappData? =
        withContext(Dispatchers.IO) {
            val safeFileName = sanitizeFileName(microappCode) + ".json"
            val file = File(storageDir, safeFileName)
            if (!file.exists()) {
                return@withContext null
            }

            try {
                val json = file.readText()
                gson.fromJson(json, CachedMicroappData::class.java)
                    ?.takeIf { it.hasData() }
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось загрузить микроапп: $microappCode", e)
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

    override suspend fun saveCollectionId(id: String) = withContext(Dispatchers.IO) {
        ensureMetaDir()
        File(metaDir, FILE_COLLECTION_ID).writeText(id)
    }

    override suspend fun getCollectionId(): String? = withContext(Dispatchers.IO) {
        migrateFromLegacyIfNeeded()
        val file = File(metaDir, FILE_COLLECTION_ID)
        if (!file.exists()) return@withContext null
        file.readText().takeIf { it.isNotBlank() }
    }

    override suspend fun saveCollectionCodes(codes: List<String>) = withContext(Dispatchers.IO) {
        ensureMetaDir()
        File(metaDir, FILE_COLLECTION_CODES).writeText(gson.toJson(codes))
    }

    override suspend fun getCollectionCodes(): List<String> = withContext(Dispatchers.IO) {
        migrateFromLegacyIfNeeded()
        readJsonList(File(metaDir, FILE_COLLECTION_CODES))
    }

    override suspend fun clearCollectionId() {
        withContext(Dispatchers.IO) {
            File(metaDir, FILE_COLLECTION_ID).delete()
            File(metaDir, FILE_COLLECTION_CODES).delete()
        }
    }

    override suspend fun addSingleListCode(code: String) = withContext(Dispatchers.IO) {
        ensureMetaDir()
        val current = readJsonList(File(metaDir, FILE_SINGLE_LIST_CODES)).toMutableSet()
        current.add(code)
        File(metaDir, FILE_SINGLE_LIST_CODES).writeText(gson.toJson(current.toList()))
    }

    override suspend fun getSingleListCodes(): List<String> = withContext(Dispatchers.IO) {
        migrateFromLegacyIfNeeded()
        readJsonList(File(metaDir, FILE_SINGLE_LIST_CODES))
    }

    override suspend fun clearSingleListCodes() {
        withContext(Dispatchers.IO) {
            File(metaDir, FILE_SINGLE_LIST_CODES).delete()
        }
    }

    override suspend fun removeCodesFromSingleList(codes: List<String>) {
        withContext(Dispatchers.IO) {
            val toRemove = codes.toSet()
            val current = readJsonList(File(metaDir, FILE_SINGLE_LIST_CODES)).toMutableSet()
            current.removeAll(toRemove)
            if (current.isEmpty()) {
                File(metaDir, FILE_SINGLE_LIST_CODES).delete()
            } else {
                File(metaDir, FILE_SINGLE_LIST_CODES).writeText(gson.toJson(current.toList()))
            }
        }
    }

    private fun ensureMetaDir() {
        metaDir.mkdirs()
    }

    private fun migrateFromLegacyIfNeeded() {
        if (File(metaDir, FILE_COLLECTION_ID).exists() ||
            File(metaDir, FILE_COLLECTION_CODES).exists() ||
            File(metaDir, FILE_SINGLE_LIST_CODES).exists()
        ) return
        val prefs = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        var migrated = false
        prefs.getString(LEGACY_KEY_COLLECTION_ID, null)?.takeIf { it.isNotBlank() }?.let { id ->
            ensureMetaDir()
            File(metaDir, FILE_COLLECTION_ID).writeText(id)
            migrated = true
        }
        prefs.getStringSet(LEGACY_KEY_COLLECTION_CODES, null)?.toList()?.takeIf { it.isNotEmpty() }
            ?.let { codes ->
                ensureMetaDir()
                File(metaDir, FILE_COLLECTION_CODES).writeText(gson.toJson(codes))
                migrated = true
            }
        prefs.getStringSet(LEGACY_KEY_SINGLE_LIST_CODES, null)?.toList()?.takeIf { it.isNotEmpty() }
            ?.let { codes ->
                ensureMetaDir()
                File(metaDir, FILE_SINGLE_LIST_CODES).writeText(gson.toJson(codes))
                migrated = true
            }
        if (migrated) {
            prefs.edit { clear() }
        }
    }

    private fun readJsonList(file: File): List<String> {
        if (!file.exists()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(file.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось прочитать ${file.name}", e)
            emptyList()
        }
    }

    private fun sanitizeFileName(code: String): String =
        code.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
}
