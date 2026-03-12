package com.example.drivenui.engine.generative_screen.action

/**
 * Обработчик внешних deeplink (открытие в браузере, приложении и т.д.).
 */
interface ExternalDeeplinkHandler {

    /**
     * Обрабатывает deeplink.
     *
     * @param deeplink URL или deeplink для открытия
     * @return true если успешно обработано, false при ошибке
     */
    suspend fun handleExternalDeeplink(deeplink: String): Boolean
}

