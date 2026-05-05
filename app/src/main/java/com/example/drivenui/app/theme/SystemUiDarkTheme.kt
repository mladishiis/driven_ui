package com.example.drivenui.app.theme

import android.content.Context
import android.content.res.Configuration

/**
 * Тёмная тема устройства: ночной UI mode в [Configuration].
 *
 * @return ночной режим включён в системе
 */
fun Context.isSystemInDarkTheme(): Boolean {
    val uiMode = resources.configuration.uiMode
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
