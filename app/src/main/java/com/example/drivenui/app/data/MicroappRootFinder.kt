package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Утилита для определения и кеширования корневой папки микроаппа в `microapps`.
 *
 * Имя корня хранится в SharedPreferences; при отсутствии папки берётся первая подпапка в `microapps`.
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
     *
     * @param context контекст приложения для доступа к SharedPreferences
     * @param dirName имя папки микроаппа
     */
    fun saveMicroappRootName(context: Context, dirName: String) {
        prefs(context).edit().putString(KEY_CURRENT_ROOT, dirName).apply()
    }

    /**
     * Очищает сохранённое имя корневой папки микроаппа.
     *
     * @param context контекст приложения для доступа к SharedPreferences
     */
    fun clearSavedMicroappRoot(context: Context) {
        prefs(context).edit().remove(KEY_CURRENT_ROOT).apply()
    }

    /**
     * Находит корневую папку микроаппа в `microapps`.
     *
     * @param context контекст приложения для доступа к файловой системе и SharedPreferences
     * @return корневая папка микроаппа или null, если не найдена
     */
    fun findMicroappRoot(context: Context): File? {
        val microappsDir = File(context.filesDir, MICROAPPS_DIR)
        if (!microappsDir.exists() || !microappsDir.isDirectory) {
            return null
        }

        val savedName = prefs(context).getString(KEY_CURRENT_ROOT, null)
        if (!savedName.isNullOrBlank()) {
            val savedDir = File(microappsDir, savedName)
            if (savedDir.exists() && savedDir.isDirectory) {
                return savedDir
            } else {
                Log.w(TAG, "Сохранённый корень микроаппа '$savedName' не найден, повторный поиск")
            }
        }

        val firstDir = microappsDir.listFiles()?.firstOrNull { it.isDirectory }
        if (firstDir != null) {
            saveMicroappRootName(context, firstDir.name)
            return firstDir
        }

        Log.w(TAG, "Не удалось определить корневой каталог микроаппа")
        return null
    }

    /**
     * Получает путь (имя папки) корня микроаппа.
     *
     * @param context контекст приложения для доступа к файловой системе
     * 
     * @return имя папки микроаппа или null, если микроапп не найден
     */
    fun getMicroappRootPath(context: Context): String? {
        return findMicroappRoot(context)?.name
    }
}
