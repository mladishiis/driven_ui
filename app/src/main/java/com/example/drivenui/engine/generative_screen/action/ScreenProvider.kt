package com.example.drivenui.engine.generative_screen.action

import com.example.drivenui.engine.generative_screen.models.ScreenModel

/**
 * Провайдер экранов для навигации.
 */
interface ScreenProvider {
    /**
     * Находит экран по коду.
     *
     * @param screenCode Код экрана
     * @return ScreenModel или null
     */
    suspend fun findScreen(screenCode: String): ScreenModel?

    /**
     * Находит экран по deeplink.
     *
     * @param deeplink Deeplink экрана
     * @return ScreenModel или null
     */
    suspend fun findScreenByDeeplink(deeplink: String): ScreenModel?
}
