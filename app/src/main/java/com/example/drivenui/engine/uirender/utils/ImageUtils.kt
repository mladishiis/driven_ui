package com.example.drivenui.engine.uirender.utils

import android.content.Context
import com.example.drivenui.app.data.MicroappRootFinder
import java.io.File

/**
 * Резолвит значение из url в источник данных для Coil:
 * - если это полный URL, возвращает его как есть;
 * - если это имя файла (например, "resources/images/close.svg"), ищет:
 *   1) во временной папке microapp: microapps/{microappName}/resources/images
 *   2) в assets: resources/images.
 *
 * @param context Контекст приложения
 * @param url URL или путь к изображению
 * @return URI, File или путь к asset; null если url пустой
 */
fun resolveImageData(context: Context, url: String?): Any? {
    if (url.isNullOrBlank()) return null

    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }

    val microappRoot = MicroappRootFinder.findMicroappRoot(context)
    if (microappRoot != null) {
        val runtimeFile = File(microappRoot, url)
        if (runtimeFile.exists()) {
            return runtimeFile
        }
    }

    return "file:///android_asset/$url"
}