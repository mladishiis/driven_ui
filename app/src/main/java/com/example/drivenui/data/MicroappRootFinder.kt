package com.example.drivenui.data

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Утилита для определения и кеширования корневой папки микроаппа в `microapps`.
 *
 * На демо-этапе в `microapps` может лежать только один микроапп, поэтому:
 * - по умолчанию корнем считаем первую найденную подпапку;
 * - один раз сохраняем её имя и дальше используем кеш, не сканируя файловую систему каждый раз.
 */
object MicroappRootFinder {
    private const val TAG = "MicroappRootFinder"
    private const val MICROAPPS_DIR = "microapps"
    private const val PREFS_NAME = "microapps_prefs"
    private const val KEY_CURRENT_ROOT = "current_microapp_root"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Сохраняет имя корневой папки микроаппа после загрузки/распаковки архива.
     */
    fun saveMicroappRootName(context: Context, dirName: String) {
        Log.d(TAG, "Saving microapp root name: $dirName")
        prefs(context).edit().putString(KEY_CURRENT_ROOT, dirName).apply()
    }

    /**
     * Очищает сохранённое имя корневой папки
     */
    fun clearSavedMicroappRoot(context: Context) {
        Log.d(TAG, "Clearing saved microapp root name")
        prefs(context).edit().remove(KEY_CURRENT_ROOT).apply()
    }

    /**
     * Находит корневую папку микроаппа в `microapps`.
     *
     * Алгоритм:
     * 1. Пробуем взять имя из сохранённого значения (SharedPreferences).
     * 2. Если не найдено или папка отсутствует — берём первую подпапку в `microapps`,
     *    считаем её корнем микроаппа и сохраняем её имя.
     *
     * @return корневая папка микроаппа или null, если не найдена.
     */
    fun findMicroappRoot(context: Context): File? {
        val microappsDir = File(context.filesDir, MICROAPPS_DIR)
        if (!microappsDir.exists() || !microappsDir.isDirectory) {
            Log.d(TAG, "Microapps directory does not exist")
            return null
        }

        // 1. Пробуем использовать сохранённое имя
        val savedName = prefs(context).getString(KEY_CURRENT_ROOT, null)
        if (!savedName.isNullOrBlank()) {
            val savedDir = File(microappsDir, savedName)
            if (savedDir.exists() && savedDir.isDirectory) {
                Log.d(TAG, "Using saved microapp root: $savedName")
                return savedDir
            } else {
                Log.w(TAG, "Saved microapp root '$savedName' not found, will rescan")
            }
        }

        // 2. Иначе берём первую подпапку как корень микроаппа
        val firstDir = microappsDir.listFiles()?.firstOrNull { it.isDirectory }
        if (firstDir != null) {
            Log.d(TAG, "Using first directory as microapp root: ${firstDir.name}")
            saveMicroappRootName(context, firstDir.name)
            return firstDir
        }

        Log.w(TAG, "Could not determine microapp root directory")
        return null
    }

    /**
     * Получает путь (имя папки) корня микроаппа.
     * Если микроапп не найден, возвращает null.
     */
    fun getMicroappRootPath(context: Context): String? {
        return findMicroappRoot(context)?.name
    }
}
