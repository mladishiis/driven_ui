package com.example.drivenui.engine.generative_screen.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Реализация [ExternalDeeplinkHandler] через Intent.ACTION_VIEW.
 *
 * @property appContext контекст приложения для запуска активности
 */
class DefaultExternalDeeplinkHandler @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ExternalDeeplinkHandler {

    override suspend fun handleExternalDeeplink(deeplink: String): Boolean {
        return try {
            val uri = deeplink.toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Log.w("ExternalDeeplinkHandler", "Нет приложения для открытия deeplink: $deeplink", e)
            false
        } catch (t: Throwable) {
            Log.e("ExternalDeeplinkHandler", "Ошибка обработки deeplink: $deeplink", t)
            false
        }
    }
}
