package com.example.drivenui.app.navigation

import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.parser.SDUIParser

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParser.ParsedMicroappResult? = null
    private var sharedMappedData: CachedMicroappData? = null
    private var templateType: String? = null
    private var templateCode: String? = null

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
     * Сохраняет информацию о шаблоне для режима скриншотов.
     */
    fun setTemplateInfo(type: String, code: String) {
        templateType = type
        templateCode = code
    }

    /**
     * Очищает параметры шаблона, сохранённые для режима загрузки скриншотов.
     */
    fun clearTemplateInfo() {
        templateType = null
        templateCode = null
    }

    /**
     * Возвращает тип и код шаблона, или null если не в режиме шаблона.
     */
    fun getTemplateInfo(): Pair<String, String>? {
        val type = templateType ?: return null
        val code = templateCode ?: return null
        return type to code
    }

    /**
     * Очищает все сохраненные данные
     */
    fun clearAllData() {
        sharedData = null
        sharedMappedData = null
        templateType = null
        templateCode = null
    }
}