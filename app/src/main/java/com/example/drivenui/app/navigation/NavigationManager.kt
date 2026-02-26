package com.example.drivenui.app.navigation

import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.parser.SDUIParser

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParser.ParsedMicroappResult? = null
    private var sharedMappedData: CachedMicroappData? = null

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
     * Очищает все сохраненные данные
     */
    fun clearAllData() {
        sharedData = null
        sharedMappedData = null
    }
}