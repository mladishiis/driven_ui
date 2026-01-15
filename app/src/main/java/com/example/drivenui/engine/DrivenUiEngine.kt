package com.example.drivenui.engine

import com.example.drivenui.engine.generative_screen.action.NativeActionExecutor
import com.example.drivenui.engine.generative_screen.action.NativeActionRegistry

/**
 * Объект для инициализации движка.
 */
object DrivenUiEngine {

    @Volatile
    private var isInitialized = false

    /**
     * Инициализирует движок с необходимыми зависимостями от хост-приложения.
     */
    fun init(
        nativeActionExecutor: NativeActionExecutor,
    ) {
        if (isInitialized) {
            return
        }
        NativeActionRegistry.register(nativeActionExecutor)
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized

    internal fun reset() {
        NativeActionRegistry.unregister()
        isInitialized = false
    }
}
