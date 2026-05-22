package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Утилита для определения и кеширования корневых папок микроаппов в `microapps`.
 *
 * Имя последнего распакованного корня хранится в SharedPreferences;
 * при отсутствии папки [findMicroappRoot] берёт первую подпапку в `microapps`.
 *
 * Для нескольких микроаппов: [registerMicroappAssets] связывает `microappCode` → имя папки,
 * [setActiveMicroappCode] — какой микроапп сейчас на экране (для [resolveActiveMicroappRoot]).
 */
object MicroappRootFinder {
    private const val TAG = "MicroappRootFinder"
    private const val MICROAPPS_DIR = "microapps"
    private const val PREFS_NAME = "microapps_prefs"
    private const val KEY_CURRENT_ROOT = "current_microapp_root"
    private const val KEY_ACTIVE_CODE = "active_microapp_code"
    private const val KEY_ASSETS_DIR_PREFIX = "assets_dir_"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Сохраняет имя корневой папки микроаппа после загрузки/распаковки архива.
     *
     * @param context контекст приложения для доступа к SharedPreferences
     * @param dirName имя папки микроаппа в `microapps/`
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
     * Запоминает папку ассетов для [microappCode] (после распаковки или парсинга).
     *
     * @param context контекст приложения
     * @param microappCode код микроаппа
     * @param assetsDirName имя подпапки в `microapps/`
     */
    fun registerMicroappAssets(context: Context, microappCode: String, assetsDirName: String) {
        if (microappCode.isBlank() || assetsDirName.isBlank()) return
        prefs(context).edit()
            .putString(KEY_ASSETS_DIR_PREFIX + microappCode, assetsDirName)
            .apply()
    }

    /**
     * Указывает, какой микроапп сейчас отображается (перед открытием экрана рендера).
     *
     * @param context контекст приложения
     * @param microappCode код микроаппа
     */
    fun setActiveMicroappCode(context: Context, microappCode: String) {
        if (microappCode.isBlank()) return
        prefs(context).edit().putString(KEY_ACTIVE_CODE, microappCode).apply()
    }

    private fun getAssetsDirNameForCode(context: Context, microappCode: String): String? =
        prefs(context).getString(KEY_ASSETS_DIR_PREFIX + microappCode, null)?.takeIf { it.isNotBlank() }

    /**
     * Корневая папка микроаппа по имени подпапки в `microapps/`.
     *
     * @param context контекст приложения
     * @param dirName имя папки
     * @return каталог или `null`, если не существует
     */
    fun findMicroappRootByDirName(context: Context, dirName: String): File? {
        if (dirName.isBlank()) return null
        val dir = File(File(context.filesDir, MICROAPPS_DIR), dirName)
        return dir.takeIf { it.exists() && it.isDirectory }
    }

    /**
     * Корень ассетов для активного микроаппа ([setActiveMicroappCode]).
     *
     * @param context контекст приложения
     * @return корневая папка микроаппа или `null`
     */
    fun resolveActiveMicroappRoot(context: Context): File? {
        val activeCode = prefs(context).getString(KEY_ACTIVE_CODE, null)?.takeIf { it.isNotBlank() }
        val dirName = activeCode?.let { getAssetsDirNameForCode(context, it) }
        return resolveMicroappRoot(
            context = context,
            assetsDirName = dirName,
            microappCode = activeCode,
        )
    }

    /**
     * Резолвит корень: сначала [assetsDirName], затем папка по [microappCode], затем [findMicroappRoot].
     *
     * @param context контекст приложения
     * @param assetsDirName имя папки из реестра
     * @param microappCode код микроаппа (fallback: папка с тем же именем)
     * @return корневая папка микроаппа или `null`
     */
    fun resolveMicroappRoot(
        context: Context,
        assetsDirName: String? = null,
        microappCode: String? = null,
    ): File? {
        assetsDirName?.takeIf { it.isNotBlank() }
            ?.let { findMicroappRootByDirName(context, it) }
            ?.let { return it }

        microappCode?.takeIf { it.isNotBlank() }
            ?.let { findMicroappRootByDirName(context, it) }
            ?.let { return it }

        return findMicroappRoot(context)
    }

    /**
     * Находит корневую папку микроаппа в `microapps` (последний распакованный или первая подпапка).
     *
     * @param context контекст приложения для доступа к файловой системе и SharedPreferences
     * @return корневая папка микроаппа или `null`, если не найдена
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
     * Имя папки корня микроаппа в `microapps/`.
     *
     * @param context контекст приложения
     * @return имя папки или `null`, если микроапп не найден
     */
    fun getMicroappRootPath(context: Context): String? =
        findMicroappRoot(context)?.name
}
