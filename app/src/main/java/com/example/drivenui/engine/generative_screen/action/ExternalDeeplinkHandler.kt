package com.example.drivenui.engine.generative_screen.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ExternalDeeplinkHandler {
    suspend fun handleExternalDeeplink(deeplink: String): Boolean
}

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
            Log.w("ExternalDeeplinkHandler", "No activity found for deeplink: $deeplink", e)
            false
        } catch (t: Throwable) {
            Log.e("ExternalDeeplinkHandler", "Error while handling deeplink: $deeplink", t)
            false
        }
    }
}

