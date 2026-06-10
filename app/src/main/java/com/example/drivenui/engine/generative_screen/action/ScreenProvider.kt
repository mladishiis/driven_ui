package com.example.drivenui.engine.generative_screen.action

import com.example.drivenui.engine.generative_screen.models.ScreenDefinition

/**
 * Провайдер экранов для навигации.
 */
interface ScreenProvider {
    /**
     * Находит экран по коду.
     *
     * @param screenCode код экрана
     * @return описание экрана или null
     */
    suspend fun findScreen(screenCode: String): ScreenDefinition?

    /**
     * Находит экран по deeplink.
     *
     * @param deeplink deeplink экрана
     * @return описание экрана или null
     */
    suspend fun findScreenByDeeplink(deeplink: String): ScreenDefinition?
}
