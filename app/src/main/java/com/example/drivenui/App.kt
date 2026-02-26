package com.example.drivenui

import android.app.Application
import com.example.drivenui.app.presentation.render.ExampleNativeActionExecutor
import com.example.drivenui.engine.DrivenUiEngine
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        DrivenUiEngine.init(
            nativeActionExecutor = ExampleNativeActionExecutor(this),
        )
    }
}
