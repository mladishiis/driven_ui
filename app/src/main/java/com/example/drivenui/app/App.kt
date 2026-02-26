package com.example.drivenui.app

import android.app.Application
import com.example.drivenui.engine.DrivenUiEngine
import com.example.drivenui.app.presentation.render.ExampleNativeActionExecutor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
internal class App : Application() {

    override fun onCreate() {
        super.onCreate()

        DrivenUiEngine.init(
            nativeActionExecutor = ExampleNativeActionExecutor(this)
        )
    }
}
