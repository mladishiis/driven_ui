package com.example.drivenui

import android.app.Application
import com.example.drivenui.app.data.AuthTokenProvider
import com.example.drivenui.app.presentation.render.ExampleNativeActionExecutor
import com.example.drivenui.engine.DrivenUiEngine
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application-класс, инициализирующий Hilt, движок SDUI и авторизацию.
 */
@HiltAndroidApp
class App : Application() {

    /**
     * Провайдер токена, используемый для предварительного запроса авторизации при запуске.
     */
    @Inject
    lateinit var authTokenProvider: AuthTokenProvider

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Выполняет стартовую инициализацию приложения.
     */
    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            authTokenProvider.prefetchToken()
        }

        DrivenUiEngine.init(
            nativeActionExecutor = ExampleNativeActionExecutor(this),
        )
    }
}
