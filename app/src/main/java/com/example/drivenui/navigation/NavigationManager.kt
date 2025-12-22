package com.example.drivenui.navigation

import com.example.drivenui.parser.SDUIParser

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParser.ParsedMicroapp? = null

    /**
     * Сохраняет данные для следующего экрана
     */
    fun setDataForNextScreen(data: SDUIParser.ParsedMicroapp) {
        sharedData = data
    }

    /**
     * Получает и очищает сохраненные данные
     */
    fun getAndClearData(): SDUIParser.ParsedMicroapp? {
        return sharedData.also { sharedData = null }
    }

    /**
     * Очищает сохраненные данные
     */
    fun clearData() {
        sharedData = null
    }
}