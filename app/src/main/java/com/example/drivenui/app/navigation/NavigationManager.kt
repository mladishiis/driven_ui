package com.example.drivenui.app.navigation

import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.parser.SDUIParser

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParser.ParsedMicroappResult? = null
    private var sharedMappedData: CachedMicroappData? = null
    private var screenshotBaseUrl: String? = null
    private var screenshotMicroappCode: String? = null

    /**
     * Сохраняет данные для следующего экрана (новая структура)
     */
    fun setDataForNextScreen(data: SDUIParser.ParsedMicroappResult) {
        sharedData = data
    }

    /**
     * Получает и очищает сохраненные данные (новая структура)
     */
    fun getAndClearData(): SDUIParser.ParsedMicroappResult? {
        return sharedData.also {
            sharedData = null
        }
    }

    /**
     * Сохраняет замапленные данные для TestRenderFragment
     */
    fun setMappedDataForNextScreen(data: CachedMicroappData) {
        sharedMappedData = data
    }

    /**
     * Получает и очищает замапленные данные
     */
    fun getAndClearMappedData(): CachedMicroappData? {
        return sharedMappedData.also {
            sharedMappedData = null
        }
    }

    /**
     * Сохраняет информацию для режима скриншотов.
     *
     * @param baseUrl базовый URL сервера, извлечённый из QR-кода (scheme + host + port)
     * @param microappCode код микроаппа для отправки скриншотов
     */
    fun setTemplateInfo(baseUrl: String, microappCode: String) {
        screenshotBaseUrl = baseUrl
        screenshotMicroappCode = microappCode
    }

    /**
     * Очищает параметры, сохранённые для режима загрузки скриншотов.
     */
    fun clearTemplateInfo() {
        screenshotBaseUrl = null
        screenshotMicroappCode = null
    }

    /**
     * Возвращает базовый URL и код микроаппа для режима скриншотов,
     * или null если не в режиме скриншотов.
     */
    fun getTemplateInfo(): Pair<String, String>? {
        val baseUrl = screenshotBaseUrl ?: return null
        val code = screenshotMicroappCode ?: return null
        return baseUrl to code
    }

    /**
     * Очищает все сохраненные данные
     */
    fun clearAllData() {
        sharedData = null
        sharedMappedData = null
        screenshotBaseUrl = null
        screenshotMicroappCode = null
    }
}